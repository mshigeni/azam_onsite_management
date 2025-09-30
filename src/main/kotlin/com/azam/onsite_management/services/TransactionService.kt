package com.azam.onsite_management.services

import com.azam.onsite_management.dto.DashboardStats
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class TransactionService(private val jdbcTemplate: JdbcTemplate) {

    fun getDashboardStats(): DashboardStats {
        val total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions", Long::class.java) ?: 7
        val magogoni = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions WHERE gate='MAGOGONI'", Long::class.java) ?: 7
        val kigamboni = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions WHERE gate='KIGAMBONI'", Long::class.java) ?: 7
        val offline = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions WHERE status='OFFLINE'", Long::class.java) ?: 7

        return DashboardStats(total, magogoni, kigamboni, offline)
    }
}