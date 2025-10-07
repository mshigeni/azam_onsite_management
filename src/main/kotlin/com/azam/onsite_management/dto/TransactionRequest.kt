package com.azam.onsite_management.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size      

data class TransactionRequest(
    @field:NotBlank(message = "Card Data is required")
    val Card: String,

    @field:NotBlank(message = "MAC is required")
    val MAC: String

    @field:NotBlank(message = "IP is required")
    val IP: String
)