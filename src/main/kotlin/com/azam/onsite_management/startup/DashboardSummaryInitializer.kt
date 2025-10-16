package com.azam.onsite_management.startup

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class DashboardSummaryInitializer(
    private val jdbcTemplate: JdbcTemplate
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(DashboardSummaryInitializer::class.java)

    override fun run(vararg args: String?) {
        try {
            // 🟢 Check if row exists
            val count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dashboard_summaries WHERE id = 1",
                Int::class.java
            ) ?: 0

            if (count == 0) {
                // 🟢 Insert fresh record if missing
                jdbcTemplate.update(
                    """
                    INSERT INTO dashboard_summaries 
                        (id, total_trxs, magogoni_trxs, kigamboni_trxs, offline_trxs, updated_at)
                    VALUES (1, 0, 0, 0, 0, ?)
                    """.trimIndent(),
                    LocalDateTime.now()
                )

                jdbcTemplate.update(
                    """
                    INSERT INTO dashboard_summaries 
                        (id, total_trxs, magogoni_trxs, kigamboni_trxs, offline_trxs, updated_at)
                    VALUES (2, 0, 0, 0, 0, ?)
                    """.trimIndent(),
                    LocalDateTime.now()
                )
                logger.info("✅ Initialized dashboard_summaries with zeros (no existing row found).")
            } else {
                // 🟢 Reset values if record already exists
                
                val updated = jdbcTemplate.update(
                    """
                    UPDATE dashboard_summaries 
                    SET total_trxs = 0,
                        magogoni_trxs = 0,
                        kigamboni_trxs = 0,
                        offline_trxs = 0,
                        updated_at = ?
                    WHERE id = 1 OR id = 2
                    """.trimIndent(),
                    LocalDateTime.now()
                )

                logger.info("🔄 Reset dashboard_summaries counters to zero. Rows affected: $updated")
            }

        } catch (ex: Exception) {
            logger.error("❌ Error initializing dashboard_summaries: ${ex.message}", ex)
        }
    }
}