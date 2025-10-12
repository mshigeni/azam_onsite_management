package com.azam.onsite_management.services

import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Service
// import kotlinx.coroutines.delay

@EnableAsync
@Service
class BackgroundTransferService(
    private val jdbcTemplate: org.springframework.jdbc.core.JdbcTemplate
) {

    @Async
    fun transferToTransactions(localReference: String) {

        println("IN BACKGROUND NOW....")

        try {
            // 1️⃣ Fetch the process transaction record
            Thread.sleep(7000)

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

            println("✅ Transaction $localReference transferred successfully.")

        } catch (ex: Exception) {
            println("⚠️ Failed to transfer transaction $localReference: ${ex.message}")
        }
    }
}