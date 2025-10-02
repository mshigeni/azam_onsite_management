package com.azam.onsite_management.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size      

data class TransactionRequest(
    @field:NotBlank(message = "Gate is required")
    val gate: String,

    @field:NotBlank(message = "Status is required")
    val status: String
)