package com.azam.onsite_management.controllers

import com.azam.onsite_management.dto.LoginRequest
import com.azam.onsite_management.dto.RegisterRequest
import com.azam.onsite_management.models.UserEntity
import com.azam.onsite_management.services.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.*
import com.azam.onsite_management.security.JwtUtil


@RestController
@RequestMapping("/api/users")
class UsersController(
    private val userService: UserService,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JwtUtil
) {

    @GetMapping("/all")
    fun getAllUsers(): Map<String, Any> {
        val users = userService.getAllUsers()  // Returns List<User>

        // You can format/limit fields before sending to frontend
        val userData = users.map { user ->
            mapOf(
                "firstName" to user.fullName,
                "lastName" to user.username,
                "email" to user.email,
                // "phone" to user.phone,
                "role" to user.role
            )
        }

        return mapOf("data" to userData)
    }

}