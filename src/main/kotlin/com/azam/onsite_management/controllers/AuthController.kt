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
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JwtUtil
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<Any> {
        return try {
            val user: UserEntity = userService.registerUser(
                username = request.username,
                rawPassword = request.password,
                email = request.email,
                fullName = request.fullName
            )
            ResponseEntity.ok(mapOf(
                "error" to 0,
                "message" to "User registered successfully", 
                "userId" to user.id
                ))
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                mapOf(
                    "error" to 1,
                    "message" to ex.message
                    ))
        }
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<Any> {
        return try {
            val auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.username, request.password)
            )

            val roles = auth.authorities.map { it.authority }
            val token = jwtUtil.generateToken(auth.name, roles)

            ResponseEntity.ok(
                mapOf(
                    "error" to 0,
                    "message" to "Login successful",
                    "first_name" to "first_name",
                    "middle_name" to "middle_name",
                    "last_name" to "last_name",
                    "roles" to roles,
                    "token" to token,
                )
            )
        } catch (ex: AuthenticationException) {
            ResponseEntity.status(200).body(
                mapOf(
                    "error" to 1,
                    "message" to "Invalid username or password"
                )
            )
        }
    }

}