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


    @Bean
    fun authenticationProvider(): AuthenticationProvider {
        val provider = DaoAuthenticationProvider()
        provider.setUserDetailsService(customUserDetailsService)
        provider.setPasswordEncoder(passwordEncoder)
        return provider
    }


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
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/login").permitAll()
                    .requestMatchers("/api/auth/register").hasRole("ADMIN")
                    .requestMatchers("/admin/**").hasRole("ADMIN")
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


}