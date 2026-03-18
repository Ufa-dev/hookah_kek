package com.hookah.kek_hookah.feature.tobacco.brand.model

import com.hookah.kek_hookah.feature.user.model.UserId

data class BrandForCreate(
    val name: String,
    val description: String,
    val strength: Long,
    val userId: UserId
)