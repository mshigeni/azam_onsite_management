package com.azam.onsite_management.controllers

import com.azam.onsite_management.services.TransactionService
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import com.azam.onsite_management.dto.TransactionRequest
import com.azam.onsite_management.models.UserEntity
import com.azam.onsite_management.services.UserService
import com.azam.onsite_management.models.Transaction

@RestController
@RequestMapping("/api/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    @PostMapping("/add")
    fun addTransaction(@Valid @RequestBody request: TransactionRequest): ResponseEntity<Any> {
        return try {
            transactionService.saveTransaction(
                gate = request.gate,
                status = request.status
            )
            ResponseEntity.ok(mapOf(
                "error" to 0,
                "message" to "Transaction added successfully", 
                ))
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                mapOf(
                    "error" to 1,
                    "message" to ex.message
                    ))
        }
    }
}