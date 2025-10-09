package com.azam.onsite_management.services

import com.azam.onsite_management.dto.TransactionRequest
import com.azam.onsite_management.utils.ReferenceGenerator
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import com.azam.onsite_management.utils.HexUtil
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.slf4j.LoggerFactory
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClientException

@Service
class TransactionProcessingService(
    private val jdbcTemplate: JdbcTemplate,
    private val restTemplate: RestTemplate = RestTemplate(),  // for calling N-Card API
    private val gateController: GateControllerService
) {

    fun processMagogoniTransaction(request: TransactionRequest, clientIp: String): Map<String, Any> {
        // 1️⃣ Check if card_uid exists
        val cardUID = HexUtil.convertAndReorderHexadecimal(request.card_uid)
        val existing = jdbcTemplate.queryForList(
            "SELECT * FROM process_trxs WHERE card_uid = ? AND status = 2 LIMIT 1",
            cardUID
        )

        val record = if (existing.isNotEmpty()) {
            existing[0]
        } else {
            // 2️⃣ Generate new reference and insert
            val local_reference = ReferenceGenerator.generateReferenceAZAM(clientIp)
            val amount = "500"
            jdbcTemplate.update(
                """
                INSERT INTO process_trxs (card_uid, local_reference, payment_ref, amount, comment, mac_addr, scanned_at, paid_at, site, created_by, updated_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                cardUID, 
                local_reference, 
                "", // payment_ref
                amount, 
                "Auto-generated",
                request.IP, 
                request.MAC, 
                request.scanned_at ?: NOW, 
                "", // paid_at 
                "MGGN" // MAGOGONI
                "SYS", 
                "SYS"
            )

            mapOf(
                "local_reference" to local_reference, 
                "amount" to amount, 
                "card_uid" to cardUID, 
                "mac_addr" to request.MAC
                )
        }

        // 2️⃣ Prepare payload (like Laravel array)
        val payload = mapOf(
            "tin" to "300100864",
            "mac_addr" to record["mac_addr"],
            "channel_ref" to record["local_reference"],
            "card_uid" to record["card_uid"],
            "pin" to "1234",
            "category" to "A01",
            "pay_option" to "KV",
            "imei_no" to record["mac_addr"],
            "owner_code" to "AZM001",
            "amount" to record["amount"],
            "price_code" to "9E8F",   // Kigamboni
            "event_code" to "E5510",
            "data" to "1234567890",
            "password" to record["mac_addr"]
        )

        try {

            val url = "http://10.20.56.3:9002/ticket_pug/purchase_ticket"

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
            val requestEntity = HttpEntity(payload, headers)

            // 3️⃣ Call N-Card API
            val response = restTemplate.postForObject(url, requestEntity, Map::class.java) as Map<*, *>

            val statusCode = (response["status_code"] as? Number)?.toInt() ?: -1
            val message = response["message"]?.toString() ?: "Unknown response"

            when (statusCode) {
                0 -> {
                    // ✅ 1. Update process_trxs status
                    jdbcTemplate.update(
                        "UPDATE process_trxs SET status = 1, updated_at = NOW() WHERE local_reference = ?",
                        localReference
                    )

                    // ✅ 2. Open gate
                    gateControllerService.open(request.ip, 0)

                    // ✅ 3. Launch background transfer
                    backgroundTransferService.transferToTransactions(localReference)

                    return mapOf(
                        "status" to "success",
                        "local_reference" to record["local_reference"],
                        "ncard_response" to response
                    )

                    println("🎉 Transaction approved and queued for transfer: $localReference")
                }
                3 -> {
                    jdbcTemplate.update(
                        "DELETE FROM process_trxs WHERE local_reference=?",
                        localReference
                    )
                    
                }
                else -> {
                    logger.warn("Unhandled status code: $statusCode - Message: $message")
                }
            }

        } catch (ex: HttpStatusCodeException) {
            logger.error("API returned HTTP ${ex.statusCode}: ${ex.responseBodyAsString}")
            throw RuntimeException("N-Card API error: ${ex.statusCode}")
        } catch (ex: RestClientException) {
            logger.error("Timeout or connection error: ${ex.message}")
            throw RuntimeException("N-Card API timeout (exceeded 7 seconds)")
        } catch (ex: Exception) {
            logger.error("Unexpected error: ${ex.message}", ex)
            throw RuntimeException("Unexpected error during transaction processing")
        }
    }
}