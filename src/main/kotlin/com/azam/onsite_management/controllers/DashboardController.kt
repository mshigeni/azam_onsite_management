package com.azam.onsite_management.controllers

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class DashboardController(
    private val messagingTemplate: SimpMessagingTemplate
) {

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

    // Optional: allow frontend test sends via /app/test
    @MessageMapping("/test")
    fun testMessage(message: String) {
        messagingTemplate.convertAndSend("/topic/dashboard", mapOf("hello" to message))
    }
}