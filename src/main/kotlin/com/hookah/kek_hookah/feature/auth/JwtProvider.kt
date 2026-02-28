package com.hookah.kek_hookah.feature.auth

import com.hookah.kek_hookah.feature.auth.model.JwtClaims
import com.hookah.kek_hookah.feature.user.model.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import kotlin.text.get

@Component
class JwtProvider(
    @Value($$"${app.security.jwt.secret}")
    private val jwtSecret: String,

    @Value($$"${app.security.jwt.access-token-expiration-ms:1800000}")
    private val accessTokenExpiration: Long,

    @Value($$"${app.security.jwt.refresh-token-expiration-ms:86400000}")
    private val refreshTokenExpiration: Long,
) {
    fun getSigningKey() = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

    fun generateAccessToken(user: User): String {
        val now = Date()
        val expiryDate = Date(now.time + accessTokenExpiration)

        return Jwts.builder()
            .subject(user.id.id.toString())
            .issuedAt(now)
            .expiration(expiryDate)
            .claim("type", "access")
            .id(UUID.randomUUID().toString())
            .signWith(getSigningKey(), Jwts.SIG.HS256)
            .compact()
    }

    fun generateRefreshToken(user: User): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshTokenExpiration)

        return Jwts.builder()
            .subject(user.id.id.toString())
            .issuedAt(now)
            .expiration(expiryDate)
            .claim("type", "refresh")
            .id(UUID.randomUUID().toString())
            .signWith(getSigningKey(), Jwts.SIG.HS256)
            .compact()
    }

    fun validateAndExtractClaims(token: String): JwtClaims? {
        return try {
            val claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .payload

            JwtClaims(
                userId = claims.subject ?: return null,
                issuedAt = claims.issuedAt ?: return null,
                expiresAt = claims.expiration ?: return null,
                type = claims["type"]?.toString()
            )
        } catch (_: Exception) {
            null
        }
    }

    fun extractJti(token: String): String? {
        return try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .payload
                .id
        } catch (_: Exception) {
            null
        }
    }

    fun generateNewAccessToken(refreshToken: String): String? {
        return validateAndExtractClaims(refreshToken)?.let { claims ->
            if (claims.type != "refresh") {
                return null
            }

            val now = Date()
            val expiryDate = Date(now.time + accessTokenExpiration)

            Jwts.builder()
                .subject(claims.userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .id(UUID.randomUUID().toString())
                .claim("type", "access")
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact()
        }
    }
}
