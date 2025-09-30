package com.azam.onsite_management.controllers

import com.azam.onsite_management.dto.DashboardStats
import com.azam.onsite_management.services.TransactionService
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class TransactionNotifier(
    private val messagingTemplate: SimpMessagingTemplate,
    private val transactionService: TransactionService
) {

    fun notifyDashboard() {
        val stats: DashboardStats = transactionService.getDashboardStats()
        messagingTemplate.convertAndSend("/topic/dashboard", stats)
    }
}