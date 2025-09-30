package com.azam.onsite_management.dto

data class DashboardStats(
    val totalTrxs: Long,
    val magogoniTrxs: Long,
    val kigamboniTrxs: Long,
    val offlineTrxs: Long
)