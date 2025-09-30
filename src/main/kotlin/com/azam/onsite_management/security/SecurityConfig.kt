package com.azam.onsite_management.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.core.userdetails.UserDetailsService
import com.azam.onsite_management.services.CustomUserDetailsService
import com.azam.onsite_management.services.UserService
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetails

@Configuration
class SecurityConfig(
    private val customUserDetailsService: CustomUserDetailsService, // ✅ inject service
    private val passwordEncoder: PasswordEncoder   // ✅ injected from AppBeansConfig
) {

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }

    // @Bean
    // fun authenticationProvider(
    //     customUserDetailsService: CustomUserDetailsService,
    //     passwordEncoder: PasswordEncoder
    // ): AuthenticationProvider {
    //     return DaoAuthenticationProvider().apply {
    //         setUserDetailsService(customUserDetailsService as UserDetailsService)
    //         setPasswordEncoder(passwordEncoder)
    //     }
    // }



    // @Bean
    // fun userDetailsService(userService: UserService): UserDetailsService {
    //     return CustomUserDetailsService(userService)
    // }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthFilter: JwtAuthFilter,
        customAuthEntryPoint: CustomAuthEntryPoint,
        customAccessDeniedHandler: CustomAccessDeniedHandler
    ): SecurityFilterChain {
        http
            .cors { } // ✅ enable CORS
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/login").permitAll()
                    .requestMatchers("/api/auth/register").hasRole("ADMIN")
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/ws/**").permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint(customAuthEntryPoint)   // handles 401
                    .accessDeniedHandler(customAccessDeniedHandler)   // handles 403
            }
            .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter::class.java)
            .sessionManagement { it.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS) }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): org.springframework.web.cors.CorsConfigurationSource {
        val configuration = org.springframework.web.cors.CorsConfiguration()
        configuration.allowedOrigins = listOf("http://azam-onsite.local")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true

        val source = org.springframework.web.cors.UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }


}