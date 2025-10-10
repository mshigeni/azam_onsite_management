package com.azam.onsite_management.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import org.springframework.http.client.SimpleClientHttpRequestFactory

@Configuration
class RestTemplateConfig {

    @Bean
    fun restTemplate(): RestTemplate {
        val factory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(7000) // milliseconds
            setReadTimeout(7000)
        }
        return RestTemplate(factory)
    }
}