package com.azam.onsite_management.services

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.slf4j.LoggerFactory

@Service
class GateControllerService(
    private val restTemplate: RestTemplate
) {

    private val logger = LoggerFactory.getLogger(GateControllerService::class.java)

    fun open(address: String, option: Int): ResponseEntity<String>? {
        val url = "http://$address/cdor.cgi?open=1&door=$option"

        val headers = HttpHeaders().apply {
            set("Authorization", "Basic YWRtaW46ODg4ODg4") // same as Laravel Basic auth
            set("Accept", "*/*")
            contentType = MediaType.APPLICATION_JSON
        }

        val request = HttpEntity<String>(headers)

        return try {
            val response = restTemplate.exchange(url, HttpMethod.GET, request, String::class.java)
            logger.info("Gate opened successfully at $address, door=$option")
            response
        } catch (ex: Exception) {
            logger.error("Failed to open gate at $address: ${ex.message}")
            null
        }
    }
}