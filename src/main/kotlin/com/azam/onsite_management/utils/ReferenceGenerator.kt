package com.azam.onsite_management.utils

import java.net.InetAddress
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object ReferenceGenerator {
    //Magogoni
    fun generateReferenceAZAM(ipAddress: String): String {
        val datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyy"))
        val uid = UUID.randomUUID().toString().uppercase(Locale.getDefault())
        val lastIpPart = ipAddress.substringAfterLast(".")
        val station = "AZM"
        return "$station$lastIpPart-$datetime-$uid"
    }

    //Kigamboni
    fun generateReferenceAZAK(ipAddress: String): String {
        val datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyy"))
        val uid = UUID.randomUUID().toString().uppercase(Locale.getDefault())
        val lastIpPart = ipAddress.substringAfterLast(".")
        val station = "AZK"
        return "$station$lastIpPart-$datetime-$uid"
    }
}