package com.azam.onsite_management.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "dashboard_summaries")
data class DashboardSummary(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val total_trxs: String,

    @Column(nullable = false)
    val magogoni_trxs: String,

    @Column(nullable = false)
    val kigamboni_trxs: String,

    @Column(nullable = false)
    val offline_trxs: String,

    @Column(nullable = false)
    val updated_at: String,

)