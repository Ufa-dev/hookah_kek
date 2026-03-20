package com.hookah.kek_hookah.feature.auth.api.dto

import com.hookah.kek_hookah.feature.user.model.UserId
import java.util.UUID


data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val refreshExpiresIn: Long
)