package com.hookah.kek_hookah.feature.auth.internal.scheduled

import com.hookah.kek_hookah.feature.auth.internal.repository.RefreshTokenRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TokenCleanupScheduler(
    private val refreshTokenRepository: RefreshTokenRepository
) {
    @Scheduled(fixedRate = 3600000)  // 1 hour
    suspend fun cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpired()
    }
}