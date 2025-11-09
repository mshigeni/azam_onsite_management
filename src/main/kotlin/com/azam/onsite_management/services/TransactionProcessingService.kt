package com.azam.onsite_management.services

import com.azam.onsite_management.dto.TransactionRequest
import com.azam.onsite_management.utils.ReferenceGenerator
import com.azam.onsite_management.utils.HexUtil
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.SocketTimeoutException
import java.time.LocalDateTime

@Service
class TransactionProcessingService(
    private val jdbcTemplate: JdbcTemplate,
    private val gateController: GateControllerService,
    private val backgroundTransferService: BackgroundTransferService
) {
    private val logger = LoggerFactory.getLogger(TransactionProcessingService::class.java)

    //MAGOGONI PROCESSING
    fun processMagogoniTransaction(request: TransactionRequest, clientIp: String): Map<String, Any> {
        
        val macAddr = request.MAC
        val sanitizedMac = formatMacAddress(macAddr)
            // Check device status from DB

        val deviceRows = jdbcTemplate.queryForList(
            "SELECT state, status FROM devices WHERE mac_addr = ? LIMIT 1",
            sanitizedMac
        )

        if (deviceRows.isEmpty()) {
            logger.error("❌ Device with MAC $macAddr not found in database")
            return mapOf("error" to 1, "message" to "Device not registered!")
        }

        val device = deviceRows[0]

        val state = when (val s = device["state"]) {
            is Number -> s.toInt()
            is String -> s.toIntOrNull() ?: throw IllegalStateException("Invalid device state format: $s")
            else -> throw IllegalStateException("Device state missing or invalid type: ${s?.javaClass}")
        }

        val status = when (val s = device["status"]) {
            is Number -> s.toInt()
            is String -> s.toIntOrNull() ?: throw IllegalStateException("Invalid device status format: $s")
            else -> throw IllegalStateException("Device status missing or invalid type: ${s?.javaClass}")
        }

        logger.info("✅ Device state: $state, Device status: $status")

        // Device busy, block new requests
        if (status == 1) {
            logger.warn("⏳ Gate $macAddr is currently processing another transaction")
            return mapOf("error" to 1, "message" to "Gate busy, please wait")
        }

        // Lock the device to prevent concurrent transactions
        jdbcTemplate.update(
            "UPDATE devices SET status = 1, updated_at = ? WHERE mac_addr = ?",
            LocalDateTime.now(),
            sanitizedMac
        )
        logger.info("🔒 Device $macAddr locked for transaction processing")

        try {
            // 4️⃣ Based on device state, trigger Online or Offline flow
            return if (state == 1) {
                logger.info("🌐 Device $macAddr is ONLINE — starting online process")
                processOnlineTransaction(request, sanitizedMac, "MGGN")
            } else {
                logger.info("⚙️ Device $macAddr is OFFLINE — starting offline process")
                processOfflineTransaction(request, sanitizedMac, "MGGN")
                // mapOf("error" to 1, "message" to "START OFFLINE PROCESS - Not Implemented")
            }

        } finally {
            // 5️⃣ Mark device back to idle after completion (success or fail)
            jdbcTemplate.update(
                "UPDATE devices SET status = 0, updated_at = ? WHERE mac_addr = ?",
                LocalDateTime.now(),
                sanitizedMac
            )
        }
    }

    //KIGAMBONI PROCESSING
    fun processKigamboniTransaction(request: TransactionRequest, clientIp: String): Map<String, Any> {
        
        val macAddr = request.MAC
        val sanitizedMac = formatMacAddress(macAddr)
            // Check device status from DB

        val deviceRows = jdbcTemplate.queryForList(
            "SELECT state, status FROM devices WHERE mac_addr = ? LIMIT 1",
            sanitizedMac
        )

        if (deviceRows.isEmpty()) {
            logger.error("❌ Device with MAC $macAddr not found in database")
            return mapOf("error" to 1, "message" to "Device not registered!")
        }

        val device = deviceRows[0]

        val state = when (val s = device["state"]) {
            is Number -> s.toInt()
            is String -> s.toIntOrNull() ?: throw IllegalStateException("Invalid device state format: $s")
            else -> throw IllegalStateException("Device state missing or invalid type: ${s?.javaClass}")
        }

        val status = when (val s = device["status"]) {
            is Number -> s.toInt()
            is String -> s.toIntOrNull() ?: throw IllegalStateException("Invalid device status format: $s")
            else -> throw IllegalStateException("Device status missing or invalid type: ${s?.javaClass}")
        }

        logger.info("✅ Device state: $state, Device status: $status")

        // Device busy, block new requests
        if (status == 1) {
            logger.warn("⏳ Gate $macAddr is currently processing another transaction")
            return mapOf("error" to 1, "message" to "Gate busy, please wait")
        }

        // Lock the device to prevent concurrent transactions
        jdbcTemplate.update(
            "UPDATE devices SET status = 1, updated_at = ? WHERE mac_addr = ?",
            LocalDateTime.now(),
            sanitizedMac
        )
        logger.info("🔒 Device $macAddr locked for transaction processing")

        try {
            // 4️⃣ Based on device state, trigger Online or Offline flow
            return if (state == 1) {
                logger.info("🌐 Device $macAddr is ONLINE — starting online process")
                processOnlineTransaction(request, sanitizedMac, "KGMBN")
            } else {
                logger.info("⚙️ Device $macAddr is OFFLINE — starting offline process")
                processOfflineTransaction(request, sanitizedMac, "KGMBN")
                // mapOf("error" to 1, "message" to "START OFFLINE PROCESS - Not Implemented")
            }

        } finally {
            // 5️⃣ Mark device back to idle after completion (success or fail)
            jdbcTemplate.update(
                "UPDATE devices SET status = 0, updated_at = ? WHERE mac_addr = ?",
                LocalDateTime.now(),
                sanitizedMac
            )
        }
    }

    fun processOnlineTransaction(request: TransactionRequest, sanitizedMac: String, deviceLocation: String): Map<String, Any> {

        val restTemplate = RestTemplate().apply {
            requestFactory = org.springframework.http.client.SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(7000)
                setReadTimeout(7000)
            }
        }

        val cardUID = HexUtil.convertAndReorderHexadecimal(request.Card.toLong())
        // 🔹 1. Check if transaction exists in process_trx
        val existing = jdbcTemplate.queryForList(
            "SELECT * FROM process_trx WHERE card_uid = ? AND `status` = 2 LIMIT 1", //status = 2 means TIMEOUT
            cardUID
        )

        val record = if (existing.isNotEmpty()) {
            existing[0]
        } else {
            // 🔹 2. Generate new local_reference and insert
            val localRef = if(deviceLocation == "MGGN"){
                ReferenceGenerator.generateReferenceAZAM(request.IP)
            } else {
                ReferenceGenerator.generateReferenceAZAK(request.IP)
            }

            val amount = "500"

            jdbcTemplate.update(
                """
                INSERT INTO process_trx 
                (card_uid, local_reference, payment_ref, amount, comment, ip_addr, mac_addr, scanned_at, paid_at, site, status, created_at,  created_by, updated_at, updated_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                cardUID, 
                localRef,
                 "",
                amount, 
                "Successfully Created",
                request.IP, 
                sanitizedMac, 
                LocalDateTime.now(), 
                "",
                deviceLocation,  //Site
                "0",     //Status
                LocalDateTime.now(), //Created At
                "SYS", 
                LocalDateTime.now(),  //Updated At
                "SYS"
            )

            mapOf(
                "local_reference" to localRef,
                "amount" to amount,
                "card_uid" to cardUID,
                "mac_addr" to request.MAC
            )
        }

        // 🔹 3. Build N-Card payload
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
            "price_code" to "9E8F",
            "event_code" to "E5510",
            "data" to "1234567890",
            "password" to record["mac_addr"]
        )

        // val url = "http://10.20.56.3:9002/ticket_pug/purchase_ticket"
        val url = "http://wanamaji.local/emulate-ncard-check"
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val requestEntity = HttpEntity(payload, headers)

        return try {
            val response = restTemplate.postForObject(url, requestEntity, Map::class.java) as Map<*, *>
            val statusCode = (response["status_code"] as? Number)?.toInt() ?: -1
            val message = response["message"]?.toString() ?: "Unknown response"
            val localRef = record["local_reference"].toString()
            val paidAt = response["paid_at"].toString()
            val paymentRef = response["ref"].toString()

            when (statusCode) {
                0 -> {
                    jdbcTemplate.update(
                        "UPDATE process_trx SET `status` = 1, comment = ?, paid_at = ?, payment_ref = ?, updated_at = ? WHERE local_reference = ?",
                        message,
                        paidAt,
                        paymentRef,
                        LocalDateTime.now(),
                        localRef
                    )

                    // gateController.open(request.IP, 0)
                    
                    backgroundTransferService.transferToTransactions(localRef)

                    logger.info("✅ Success [$localRef]: N-Card approved transaction.")
                    mapOf("error" to 0, "message" to message, "local_reference" to localRef)
                }

                302, 320, 3 -> {
                    jdbcTemplate.update("DELETE FROM process_trx WHERE local_reference = ?", localRef)
                    logger.warn("⚠️ Deleted transaction $localRef due to status $statusCode ($message)")
                    mapOf("error" to 1, "message" to message)
                }

                else -> {
                    logger.warn("Unhandled status code $statusCode: $message")
                    mapOf("error" to 1, "message" to message)
                }
            }

        } catch (ex: SocketTimeoutException) {
            val localRef = record["local_reference"].toString()
            jdbcTemplate.update("UPDATE process_trx SET `status` = 2, updated_at = ? WHERE local_reference = ?", LocalDateTime.now(), localRef)
            logger.error("⏳ Timeout while processing $localRef: ${ex.message}")
            mapOf("error" to 1, "message" to "N-Card API timeout: ${ex.message}")

        } catch (ex: Exception) {
            val localRef = record["local_reference"].toString()
            jdbcTemplate.update("UPDATE process_trx SET `status` = 2, updated_at = ? WHERE local_reference = ?", LocalDateTime.now(), localRef)
            logger.error("❌ Unexpected error while processing $localRef: ${ex.message}", ex)
            mapOf("error" to 1, "message" to "Unexpected error: ${ex.message}")
        }
    }

    fun processOfflineTransaction(request: TransactionRequest, sanitizedMac: String, deviceLocation: String): Map<String, Any> {

        val restTemplate = RestTemplate().apply {
            requestFactory = org.springframework.http.client.SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(7000)
                setReadTimeout(7000)
            }
        }

        val cardUID = HexUtil.convertAndReorderHexadecimal(request.Card.toLong())
        // 🔹 1. Check if transaction exists in process_trx
        val existing = jdbcTemplate.queryForList(
            "SELECT * FROM process_trx WHERE card_uid = ? AND `status` = 2 LIMIT 1", //status = 2 means TIMEOUT
            cardUID
        )

        val record = if (existing.isNotEmpty()) {
            existing[0]
        } else {
            // 🔹 2. Generate new local_reference and insert
            val localRef = if(deviceLocation == "MGGN"){
                ReferenceGenerator.generateReferenceAZAM(request.IP)
            } else {
                ReferenceGenerator.generateReferenceAZAK(request.IP)
            }
            
            val amount = "500"

            jdbcTemplate.update(
                """
                INSERT INTO process_trx 
                (card_uid, local_reference, payment_ref, amount, comment, ip_addr, mac_addr, scanned_at, paid_at, site, status, created_at,  created_by, updated_at, updated_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                cardUID, 
                localRef,
                 "",
                amount, 
                "Successfully Created",
                request.IP, 
                sanitizedMac, 
                LocalDateTime.now(), 
                "",
                deviceLocation,  //Site
                "0",     //Status
                LocalDateTime.now(), //Created At
                "SYS", 
                LocalDateTime.now(),  //Updated At
                "SYS"
            )

            mapOf(
                "local_reference" to localRef,
                "amount" to amount,
                "card_uid" to cardUID,
                "mac_addr" to request.MAC
            )
        }

        // 🔹 3. Build N-Card payload
        val payload = mapOf(
            "card_uid" to record["card_uid"],
        )

        // val url = "http://10.20.56.3:9002/ticket_pug/purchase_ticket"
        val url = "http://wanamaji.local/emulate-ncard-check"
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val requestEntity = HttpEntity(payload, headers)

        return try {
            val response = restTemplate.postForObject(url, requestEntity, Map::class.java) as Map<*, *>
            val statusCode = (response["status_code"] as? Number)?.toInt() ?: -1
            val message = response["message"]?.toString() ?: "Unknown response"
            val localRef = record["local_reference"].toString()
            val paidAt = response["paid_at"].toString()
            val paymentRef = response["ref"].toString()

            when (statusCode) {
                0 -> {
                    jdbcTemplate.update(
                        "UPDATE process_trx SET `status` = 1, comment = ?, paid_at = ?, payment_ref = ?, updated_at = ? WHERE local_reference = ?",
                        message,
                        paidAt,
                        paymentRef,
                        LocalDateTime.now(),
                        localRef
                    )

                    // gateController.open(request.IP, 0)
                    
                    backgroundTransferService.transferToTransactions(localRef)

                    logger.info("✅ Success [$localRef]: N-Card approved transaction.")
                    mapOf("error" to 0, "message" to message, "local_reference" to localRef)
                }

                302, 320, 3 -> {
                    jdbcTemplate.update("DELETE FROM process_trx WHERE local_reference = ?", localRef)
                    logger.warn("⚠️ Deleted transaction $localRef due to status $statusCode ($message)")
                    mapOf("error" to 1, "message" to message)
                }

                else -> {
                    logger.warn("Unhandled status code $statusCode: $message")
                    mapOf("error" to 1, "message" to message)
                }
            }

        } catch (ex: SocketTimeoutException) {
            val localRef = record["local_reference"].toString()
            jdbcTemplate.update("UPDATE process_trx SET `status` = 2, updated_at = ? WHERE local_reference = ?", LocalDateTime.now(), localRef)
            logger.error("⏳ Timeout while processing $localRef: ${ex.message}")
            mapOf("error" to 1, "message" to "N-Card API timeout: ${ex.message}")

        } catch (ex: Exception) {
            val localRef = record["local_reference"].toString()
            jdbcTemplate.update("UPDATE process_trx SET `status` = 2, updated_at = ? WHERE local_reference = ?", LocalDateTime.now(), localRef)
            logger.error("❌ Unexpected error while processing $localRef: ${ex.message}", ex)
            mapOf("error" to 1, "message" to "Unexpected error: ${ex.message}")
        }
    }

    fun formatMacAddress(mac: String): String {
        return mac.chunked(2).joinToString(":").uppercase()
    }
}