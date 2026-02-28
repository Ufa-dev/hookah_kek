package com.hookah.kek_hookah.feature.user.model

data class UserForUpdate(
    val userId: UserId,
    val name: String? = null,
    val email: String? = null
)
