package com.azam.onsite_management.startup

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class DeviceStatusInitializer(
    private val jdbcTemplate: JdbcTemplate
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(DeviceStatusInitializer::class.java)

    override fun run(vararg args: String?) {
        try {
            val updated = jdbcTemplate.update("UPDATE devices SET status = 0")
            logger.info("✅ All devices reset to IDLE (status=0) at application startup. Rows affected: $updated")
        } catch (ex: Exception) {
            logger.error("❌ Error resetting device statuses: ${ex.message}", ex)
        }
    }
}