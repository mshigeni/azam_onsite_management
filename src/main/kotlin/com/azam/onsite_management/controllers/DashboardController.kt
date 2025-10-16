package com.azam.onsite_management.controllers

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import com.azam.onsite_management.services.TransactionService
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam

@RestController
@RequestMapping("/api/dashboard")
class DashboardController(
    private val messagingTemplate: SimpMessagingTemplate,
    private val transactionService: TransactionService
) {

    @GetMapping("/summary")
    fun getDashboardSummary(): Map<String, Any> {
        val summary = transactionService.getSummary()
        return mapOf(
            "totalTrxs" to summary.totalTrxs,
            "magogoniTrxs" to summary.magogoniTrxs,
            "kigamboniTrxs" to summary.kigamboniTrxs,
            "offlineTrxs" to summary.offlineTrxs,
            "totalAmount" to summary.totalAmount,
            "magogoniAmount" to summary.magogoniAmount,
            "kigamboniAmount" to summary.kigamboniAmount,
            "offlineAmount" to summary.offlineAmount
        )
    }

    // Optional: allow frontend test sends via /app/test
    @MessageMapping("/test")
    fun testMessage(message: String) {
        messagingTemplate.convertAndSend("/topic/dashboard", mapOf("hello" to message))
    }
}