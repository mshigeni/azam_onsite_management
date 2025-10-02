package com.azam.onsite_management.services

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service 
import com.azam.onsite_management.models.Transaction
import com.azam.onsite_management.controllers.DashboardController

data class TransactionSummary(
    val totalTrxs: Long,
    val magogoniTrxs: Long,
    val kigamboniTrxs: Long,
    val offlineTrxs: Long
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
        val magogoni = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions WHERE gate='MAGOGONI'", Long::class.java) ?: 0
        val kigamboni = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions WHERE gate='KIGAMBONI'", Long::class.java) ?: 0
        val offline = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions WHERE status='OFFLINE'", Long::class.java) ?: 0

        return TransactionSummary(total, magogoni, kigamboni, offline)
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