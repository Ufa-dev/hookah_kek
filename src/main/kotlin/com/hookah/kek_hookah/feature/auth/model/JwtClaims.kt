package com.hookah.kek_hookah.feature.auth.model

import java.util.*

data class JwtClaims(
    val userId: String,
    val issuedAt: Date,
    val expiresAt: Date,
    val type: String? = null
)