package com.hookah.kek_hookah.feature.auth.model

import com.hookah.kek_hookah.feature.user.model.UserId
import java.time.OffsetDateTime

data class Password(
    val userId: UserId,
    val password: String,
    val generatedAt: OffsetDateTime
)