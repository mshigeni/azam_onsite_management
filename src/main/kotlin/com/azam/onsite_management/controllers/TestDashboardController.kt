package com.azam.onsite_management.controllers

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestDashboardController(
    private val messagingTemplate: SimpMessagingTemplate
) {

    @GetMapping("/api/test/dashboard")
    fun sendTestDashboardUpdate(): Map<String, Any> {
        val payload = mapOf(
            "totalTrxs" to 100,
            "magogoniTrxs" to 40,
            "kigamboniTrxs" to 50,
            "offlineTrxs" to 10
        )

        messagingTemplate.convertAndSend("/topic/dashboard", payload)

        return mapOf("status" to "sent", "payload" to payload)
    }
}