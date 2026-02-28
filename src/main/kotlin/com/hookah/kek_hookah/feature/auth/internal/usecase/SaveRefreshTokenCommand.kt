package com.hookah.kek_hookah.feature.auth.internal.usecase

import com.hookah.kek_hookah.feature.auth.internal.repository.RefreshTokenRepository
import com.hookah.kek_hookah.feature.auth.model.JwtId
import com.hookah.kek_hookah.feature.auth.model.RefreshToken
import com.hookah.kek_hookah.feature.user.model.UserId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class SaveRefreshTokenCommand(
    private val repository: RefreshTokenRepository,

    @Value($$"${app.security.jwt.refresh-token-expiration-ms:86400000}")
    private val refreshTokenExpiration: Long,
    ) {
    suspend fun execute(userId: UserId, jti: String) {
        val expiresAt = OffsetDateTime.now().plusSeconds(refreshTokenExpiration / 1000)

        repository.save(
            RefreshToken(
                id = JwtId(),
                userId = userId,
                jti = jti,
                expiresAt = expiresAt
            )
        )
    }

}
