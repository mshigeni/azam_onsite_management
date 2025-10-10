package com.azam.onsite_management.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "process_trx")
data class ProcessTrx(

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
    val ip_addr: String,

    @Column(nullable = false)
    val mac_addr: String,

    @Column(nullable = false)
    val scanned_at: String,

    @Column(nullable = false)
    val paid_at: String,

    @Column(nullable = false)
    val site: String,   // e.g., "MAGOGONI", "KIGAMBONI"

    @Column(nullable = false)
    val status: String, // Trasanction type eg. 0 - Just tapped, 3 - powercut, 4 - timeout, 1 - success, 2 - failed maybe no balance

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_by", nullable = false)
    val createdBy: String,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_by", nullable = false)
    val updatedBy: String
)