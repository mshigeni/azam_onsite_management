package com.azam.onsite_management.services

import com.azam.onsite_management.models.Role
import com.azam.onsite_management.models.UserEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val jdbcTemplate: JdbcTemplate,
    private val passwordEncoder: PasswordEncoder
) {

    private val rowMapper = RowMapper<UserEntity> { rs, _ ->
        UserEntity(
            id = rs.getLong("id"),
            username = rs.getString("username"),
            password = rs.getString("password"),
            email = rs.getString("email"),
            fullName = rs.getString("full_name"),
            role = Role.valueOf(rs.getString("role"))
        )
    }

    fun registerUser(username: String, rawPassword: String, email: String, fullName: String): UserEntity {
        // check duplicates
        val existing = jdbcTemplate.query("SELECT * FROM users WHERE username=? OR email=?", rowMapper, username, email)
        if (existing.isNotEmpty()) {
            throw IllegalArgumentException("Username or Email already exists")
        }

        val encodedPassword = passwordEncoder.encode(rawPassword)
        jdbcTemplate.update(
            "INSERT INTO users (username, password, email, full_name, role) VALUES (?, ?, ?, ?, ?)",
            username, encodedPassword, email, fullName, Role.USER.name
        )

        // fetch the newly created user
        return jdbcTemplate.queryForObject("SELECT * FROM users WHERE username=?", rowMapper, username)!!
    }

    fun findByUsername(username: String): UserEntity? =
        jdbcTemplate.query("SELECT * FROM users WHERE username=?", rowMapper, username).firstOrNull()
}
