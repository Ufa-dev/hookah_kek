package com.hookah.kek_hookah.feature.auth.api.dto


data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val refreshExpiresIn: Long
)