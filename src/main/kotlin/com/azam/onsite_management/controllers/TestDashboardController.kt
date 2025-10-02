package com.azam.onsite_management.controllers

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam

@RestController
class TestDashboardController(
    private val messagingTemplate: SimpMessagingTemplate
) {

    @GetMapping("/api/test/dashboard")
    fun sendTestDashboardUpdate(
        @RequestParam totalTrxs: Int,
        @RequestParam magogoniTrxs: Int,
        @RequestParam kigamboniTrxs: Int,
        @RequestParam offlineTrxs: Int
    ): Map<String, Any> {
        val payload = mapOf(
            "totalTrxs" to totalTrxs,
            "magogoniTrxs" to magogoniTrxs,
            "kigamboniTrxs" to kigamboniTrxs,
            "offlineTrxs" to offlineTrxs
        )

        messagingTemplate.convertAndSend("/topic/dashboard", payload)

        return mapOf("status" to "sent", "payload" to payload)
    }

}