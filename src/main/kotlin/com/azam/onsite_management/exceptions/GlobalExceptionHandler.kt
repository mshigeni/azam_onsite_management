package com.azam.onsite_management.exceptions

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    // Handles @Valid body validation errors
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = ex.bindingResult.fieldErrors.associate { error ->
            error.field to (error.defaultMessage ?: "Invalid value")
        }
        return ResponseEntity(
            mapOf(
                    "error" to "1",
                    "message" to errors), HttpStatus.BAD_REQUEST)
    }

    // Handles @Validated on query params / path variables
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<Map<String, Any>> {
        val errors = ex.constraintViolations.associate { violation ->
            violation.propertyPath.toString() to violation.message
        }
        return ResponseEntity(
            mapOf(
                    "error" to "1",
                    "message" to errors), HttpStatus.BAD_REQUEST)
    }

    // Handles malformed JSON or missing fields
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonParseError(ex: HttpMessageNotReadableException): ResponseEntity<Map<String, Any>> {
        return ResponseEntity(
            mapOf(
                    "error" to "1",
                    "message" to listOf("Malformed JSON or missing required fields")),
            HttpStatus.BAD_REQUEST
        )
    }

    // Catch-all fallback
    @ExceptionHandler(Exception::class)
    fun handleGeneralError(ex: Exception): ResponseEntity<Map<String, Any>> {
        return ResponseEntity(
            mapOf(
                    "error" to "1",
                    "message" to listOf(ex.message ?: "Unexpected error")),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}