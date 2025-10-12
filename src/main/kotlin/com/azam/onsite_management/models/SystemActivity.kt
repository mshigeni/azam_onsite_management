package com.azam.onsite_management.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "system_activities")
data class SystemActivity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val activity: String,

    @Column(nullable = false)
    val details: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_by", nullable = false)
    val createdBy: String,

)