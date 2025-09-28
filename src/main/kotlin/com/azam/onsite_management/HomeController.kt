package com.azam.onsite_management

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class HomeController {

    @GetMapping("/")
    @ResponseBody
    fun home(): String {
        return "Welcome to Azam Onsite Management 🚀"
    }

    @GetMapping("/public/hello")
    @ResponseBody
    fun publicHello(): String {
        return "Hello from public endpoint!"
    }

    @GetMapping("/admin/dashboard")
    @ResponseBody
    fun adminDashboard(): String {
        return "Admin dashboard (requires ADMIN role)"
    }
}
