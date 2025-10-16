package com.azam.onsite_management.services

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service 
import com.azam.onsite_management.models.Transaction
import com.azam.onsite_management.controllers.DashboardController
import java.time.LocalDateTime
import java.util.Locale
import java.text.NumberFormat

data class TransactionSummary(
    val totalTrxs: String,
    val magogoniTrxs: String,
    val kigamboniTrxs: String,
    val offlineTrxs: String,
    val totalAmount: String,
    val magogoniAmount: String,
    val kigamboniAmount: String,
    val offlineAmount: String
)

@Service
class TransactionService(
    private val jdbcTemplate: JdbcTemplate,
    private val messagingTemplate: SimpMessagingTemplate
) {

    fun saveTransaction(gate: String, status: String) {
        // 1️⃣ Save transaction
        jdbcTemplate.update(
            "INSERT INTO transactions (gate, status, created_at) VALUES (?, ?, NOW())",
            gate, status
        )

        // 2️⃣ Fetch stats after insert
        val totalTrxs = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions", Int::class.java) ?: 0
        val magogoniTrxs = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions WHERE gate='MAGOGONI'", Int::class.java) ?: 0
        val kigamboniTrxs = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions WHERE gate='KIGAMBONI'", Int::class.java) ?: 0
        val offlineTrxs = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions WHERE status='OFFLINE'", Int::class.java) ?: 0

        // val payload = mapOf(
        //     "totalTrxs" to totalTrxs,
        //     "magogoniTrxs" to magogoniTrxs,
        //     "kigamboniTrxs" to kigamboniTrxs,
        //     "offlineTrxs" to offlineTrxs
        // )

        // // 3️⃣ Push to dashboard topic
        // messagingTemplate.convertAndSend("/topic/dashboard", payload)
        broadcastDashboardUpdate(totalTrxs, magogoniTrxs, kigamboniTrxs, offlineTrxs)  
    }

    fun getSummary(): TransactionSummary {
        val total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions", Long::class.java) ?: 0
        val magogoni = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions WHERE site ='MGGN'", Long::class.java) ?: 0
        val kigamboni = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions WHERE site ='KGMBN'", Long::class.java) ?: 0
        val offline = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM offline_trxs", Long::class.java) ?: 0

        // 💰 Sum totals (safe null handling using COALESCE)
        val totalAmount = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(amount), 0) FROM transactions", Double::class.java) ?: 0.0
        val magogoniAmount = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE site = 'MGGN'", Double::class.java) ?: 0.0
        val kigamboniAmount = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE site = 'KGMBN'", Double::class.java) ?: 0.0
        val offlineAmount = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(amount), 0) FROM offline_trxs", Double::class.java) ?: 0.0

        // Update dashboard_summaries table
        jdbcTemplate.update(
            """
            UPDATE dashboard_summaries
            SET 
                total_trxs = ?,
                magogoni_trxs = ?,
                kigamboni_trxs = ?,
                offline_trxs = ?,
                updated_at = ?
            WHERE id = 1
            """.trimIndent(),
            total,
            magogoni,
            kigamboni,
            offline,
            LocalDateTime.now()
        )

        // Update the second summary record with amounts
        jdbcTemplate.update(
            """
            UPDATE dashboard_summaries
            SET 
                total_trxs = ?,
                magogoni_trxs = ?,
                kigamboni_trxs = ?,
                offline_trxs = ?,
                updated_at = ?
            WHERE id = 2
            """.trimIndent(),
            totalAmount,
            magogoniAmount,
            kigamboniAmount,
            offlineAmount,
            LocalDateTime.now()
        )

        // 💡 Create a reusable formatter
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 2     // ensures e.g., 1,234.56
            minimumFractionDigits = 0
        }

        return TransactionSummary(
            totalTrxs = formatter.format(toNumber(total)),
            magogoniTrxs = formatter.format(toNumber(magogoni)),
            kigamboniTrxs = formatter.format(toNumber(kigamboni)),
            offlineTrxs = formatter.format(toNumber(offline)),
            totalAmount = formatter.format(toNumber(totalAmount)),
            magogoniAmount = formatter.format(toNumber(magogoniAmount)),
            kigamboniAmount = formatter.format(toNumber(kigamboniAmount)),
            offlineAmount = formatter.format(toNumber(offlineAmount))
        )
    }

    // 🧾 Safely convert DB values to Number (handles nulls, BigDecimal, etc.)
    fun toNumber(value: Any?): Number {
        return when (value) {
            is Number -> value
            is String -> value.toDoubleOrNull() ?: 0
            else -> 0
        }
    }

    // This method can be called when new transaction data is saved in DB
    fun broadcastDashboardUpdate(totalTrxs: Int, magogoniTrxs: Int, kigamboniTrxs: Int, offlineTrxs: Int) {
        val payload = mapOf(
            "totalTrxs" to totalTrxs,
            "magogoniTrxs" to magogoniTrxs,
            "kigamboniTrxs" to kigamboniTrxs,
            "offlineTrxs" to offlineTrxs
        )

        // ✅ Push update to all clients subscribed to /topic/dashboard
        messagingTemplate.convertAndSend("/topic/dashboard", payload)
    }
}