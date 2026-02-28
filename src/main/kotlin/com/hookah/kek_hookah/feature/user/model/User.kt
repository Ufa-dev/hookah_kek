package com.hookah.kek_hookah.feature.user.model

import java.time.OffsetDateTime

data class User(
    val id: UserId,
    val name: String,
    val email: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

