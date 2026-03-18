package com.hookah.kek_hookah.feature.tobacco.brand.model

import com.hookah.kek_hookah.feature.user.model.UserId

data class BrandForUpdate(
    val brandId: BrandId,
    val name: String,
    val description: String,
    val strength: Long,
    val userId: UserId
)