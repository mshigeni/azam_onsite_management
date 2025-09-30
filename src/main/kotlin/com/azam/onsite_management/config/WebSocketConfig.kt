package com.azam.onsite_management.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.slf4j.LoggerFactory

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    
    private val log = LoggerFactory.getLogger(WebSocketConfig::class.java)

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        // ✅ clients subscribe here
        config.enableSimpleBroker("/topic")
        // ✅ messages sent with /app prefix will go to controllers
        config.setApplicationDestinationPrefixes("/app")

        log.info("✅ WebSocket message broker configured with /topic and /app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("http://azam-onsite.local") // ✅ allow your frontend domain
            .withSockJS()

        log.info("✅ WebSocket endpoint registered at /ws")
    }
}