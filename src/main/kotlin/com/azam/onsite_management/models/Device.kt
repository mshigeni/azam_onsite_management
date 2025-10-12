package com.azam.onsite_management.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "devices")
data class Device(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val ip_addr: String,

    @Column(nullable = false)
    val mac_addr: String,

    @Column(nullable = false)
    val status: String, // e.g., "Processing" = 1, "Idle" = 0

    @Column(nullable = false)
    val state: String, // e.g., "ONLINE", "OFFLINE"

    @Column(nullable = false)
    val site: String,   // e.g., "MAGOGONI", "KIGAMBONI" etc

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_by", nullable = false)
    val createdBy: String,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_by", nullable = false)
    val updatedBy: String
)