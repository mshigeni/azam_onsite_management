package com.azam.onsite_management.services

import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Service
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.time.LocalDateTime
import java.util.Locale
import java.text.NumberFormat
// import kotlinx.coroutines.delay

@EnableAsync
@Service
class BackgroundTransferService(
    private val jdbcTemplate: org.springframework.jdbc.core.JdbcTemplate,
    private val messagingTemplate: SimpMessagingTemplate,
) {

    @Async
    fun transferToTransactions(localReference: String) {

        println("IN BACKGROUND NOW....")

        try {
            // 1️⃣ Fetch the process transaction record
            Thread.sleep(1200)

             println("ℹ️ Starting transfer for transaction with local reference: $localReference")
            val trx = jdbcTemplate.queryForMap(
                "SELECT * FROM process_trx WHERE local_reference = ?",
                localReference
            )

            // 2️⃣ Insert into main transactions table
            jdbcTemplate.update(
                """
                INSERT INTO transactions 
                (card_uid, local_reference, payment_ref, amount, comment, ip_addr, mac_addr, scanned_at, paid_at, site, type, created_at,  created_by, updated_at, updated_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                trx["card_uid"], 
                trx["local_reference"], 
                trx["payment_ref"],
                trx["amount"],
                trx["comment"],
                trx["ip_addr"],
                trx["mac_addr"],
                trx["scanned_at"],
                trx["paid_at"],
                trx["site"], //Site
                "1",     //type ONLINE = 1, OFFLINE = 0
                trx["created_at"],
                trx["created_by"],  
                trx["updated_at"],
                trx["updated_by"]
            )

            // 3️⃣ Delete from process_trxs
            jdbcTemplate.update(
                "DELETE FROM process_trx WHERE local_reference = ?",
                localReference
            )

            jdbcTemplate.update("""
                UPDATE dashboard_summaries
                SET total_trxs = total_trxs + 1,
                    magogoni_trxs = magogoni_trxs + CASE WHEN ? = 'MGGN' THEN 1 ELSE 0 END,
                    kigamboni_trxs = kigamboni_trxs + CASE WHEN ? = 'KGMBN' THEN 1 ELSE 0 END
                WHERE id = 1
            """, trx["site"], trx["site"])
            
            jdbcTemplate.update("""
                UPDATE dashboard_summaries
                SET total_trxs = total_trxs + 500,
                    magogoni_trxs = magogoni_trxs + CASE WHEN ? = 'MGGN' THEN 500 ELSE 0 END,
                    kigamboni_trxs = kigamboni_trxs + CASE WHEN ? = 'KGMBN' THEN 500 ELSE 0 END
                WHERE id = 2
            """, trx["site"], trx["site"])

            val summary = jdbcTemplate.queryForMap("SELECT * FROM dashboard_summaries WHERE id = 1")
            val summary_amount = jdbcTemplate.queryForMap("SELECT * FROM dashboard_summaries WHERE id = 2")

            // 💡 Create a reusable formatter
            val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
                maximumFractionDigits = 2     // ensures e.g., 1,234.56
                minimumFractionDigits = 0
            }

            // 🧮 Build payload with formatted numbers
            val payload = mapOf(
                "totalTrxs" to formatter.format(toNumber(summary["total_trxs"])),
                "magogoniTrxs" to formatter.format(toNumber(summary["magogoni_trxs"])),
                "kigamboniTrxs" to formatter.format(toNumber(summary["kigamboni_trxs"])),
                "offlineTrxs" to formatter.format(toNumber(summary["offline_trxs"])),

                "totalAmount" to formatter.format(toNumber(summary_amount["total_trxs"])),
                "magogoniAmount" to formatter.format(toNumber(summary_amount["magogoni_trxs"])),
                "kigamboniAmount" to formatter.format(toNumber(summary_amount["kigamboni_trxs"])),
                "offlineAmount" to formatter.format(toNumber(summary_amount["offline_trxs"]))
            )


            messagingTemplate.convertAndSend("/topic/dashboard", payload)

            println("✅ Transaction $localReference transferred successfully.")

        } catch (ex: Exception) {
            println("⚠️ Failed to transfer transaction $localReference: ${ex.message}")
        }
    }

    // 🧾 Safely convert DB values to Number (handles nulls, BigDecimal, etc.)
    fun toNumber(value: Any?): Number {
        return when (value) {
            is Number -> value
            is String -> value.toDoubleOrNull() ?: 0
            else -> 0
        }
    }
}