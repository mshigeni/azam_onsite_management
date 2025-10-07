package com.azam.onsite_management.services

import com.azam.onsite_management.models.UserEntity
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userService: UserService
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userService.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")

        return org.springframework.security.core.userdetails.User
            .withUsername(user.username)
            .password(user.password) // already encoded in DB
            .roles(user.role.name)   // Role.USER / Role.ADMIN
            .build()
    }
}
