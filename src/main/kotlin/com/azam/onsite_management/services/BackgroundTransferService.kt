

import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Service

@EnableAsync
@Service
class BackgroundTransferService(
    private val jdbcTemplate: org.springframework.jdbc.core.JdbcTemplate
) {

    @Async
    fun transferToTransactions(localReference: String) {
        try {
            // 1️⃣ Fetch the process transaction record
            val trx = jdbcTemplate.queryForMap(
                "SELECT * FROM process_trxs WHERE local_reference = ?",
                localReference
            )

            // 2️⃣ Insert into main transactions table
            jdbcTemplate.update(
                """
                INSERT INTO transactions (card_uid, reference, amount, mac_addr, gate, created_at, status)
                VALUES (?, ?, ?, ?, ?, NOW(), ?)
                """.trimIndent(),
                trx["card_uid"],
                trx["local_reference"],
                trx["amount"],
                trx["mac_addr"],
                trx["site"],
                "COMPLETED"
            )

            // 3️⃣ Delete from process_trxs
            jdbcTemplate.update(
                "DELETE FROM process_trxs WHERE local_reference = ?",
                localReference
            )

            println("✅ Transaction $localReference transferred successfully.")

        } catch (ex: Exception) {
            println("⚠️ Failed to transfer transaction $localReference: ${ex.message}")
        }
    }
}