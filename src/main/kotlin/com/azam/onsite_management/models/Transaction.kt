package com.azam.onsite_management.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "transactions")
data class Transaction(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val site: String,   // e.g., "MAGOGONI", "KIGAMBONI"

    @Column(nullable = false)
    val status: String, // e.g., "ONLINE", "OFFLINE"

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "created_by", nullable = false)
    val createdBy: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_by", nullable = false)
    val updatedBy: LocalDateTime = LocalDateTime.now()
)