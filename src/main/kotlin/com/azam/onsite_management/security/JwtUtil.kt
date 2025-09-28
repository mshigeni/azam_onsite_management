package com.azam.onsite_management.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtil {

    // Secret key (for demo; in prod use environment variable or vault)
    private val key = Keys.secretKeyFor(SignatureAlgorithm.HS256)

    fun generateToken(username: String, roles: List<String>): String {
        val now = Date()
        val expiry = Date(now.time + 1000 * 60 * 60) // 1 hour

        return Jwts.builder()
            .setSubject(username)
            .claim("roles", roles)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
            true
        } catch (ex: Exception) {
            false
        }
    }

    fun extractUsername(token: String): String {
        val claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
        return claims.subject
    }
}