package com.azam.onsite_management.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class CustomAccessDeniedHandler : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_FORBIDDEN
        val body = mapOf(
            "error" to 1,
            "message" to "Forbidden: You don’t have permission to access this resource"
        )
        response.writer.write(ObjectMapper().writeValueAsString(body))
    }
}