package com.azam.onsite_management

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync // ✅ enables @Async background services like BackgroundTransferService
class OnsiteManagementApplication

fun main(args: Array<String>) {
    runApplication<OnsiteManagementApplication>(*args)
}