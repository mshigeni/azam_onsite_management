package com.azam.onsite_management.models

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    @get:NotBlank
    val username: String,

    @Column(nullable = false)
    @get:NotBlank
    val password: String,

    @Column(nullable = false, unique = true)
    @get:Email
    val email: String,

    @Column(nullable = false)
    @get:NotBlank
    val fullName: String,

    @Enumerated(EnumType.STRING)          // ✅ store enum as text ("USER", "ADMIN")
    val role: Role = Role.USER
)
