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
    val card_uid: String,

    @Column(nullable = false)
    val local_reference: String,

    @Column(nullable = false)
    val payment_ref: String,

    @Column(nullable = false)
    val amount: String,

    @Column(nullable = false)
    val comment: String,

    @Column(nullable = false)
    val mac_addr: String,

    @Column(nullable = false)
    val scanned_at: String,

    @Column(nullable = false)
    val paid_at: String,

    @Column(nullable = false)
    val site: String,   // e.g., "MAGOGONI", "KIGAMBONI"

    @Column(nullable = false)
    val type: String, // Trasanction type eg. "ONLINE" or "1", "OFFLINE" or "0"

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_by", nullable = false)
    val createdBy: String,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_by", nullable = false)
    val updatedBy: String
)