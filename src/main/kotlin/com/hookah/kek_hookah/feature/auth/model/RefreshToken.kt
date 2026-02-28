package com.hookah.kek_hookah.feature.auth.model

import com.hookah.kek_hookah.feature.user.model.UserId
import java.time.OffsetDateTime

data class RefreshToken(
    val id: JwtId,
    val userId: UserId,
    val jti: String,
    val expiresAt: OffsetDateTime,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)