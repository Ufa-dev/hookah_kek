package com.hookah.kek_hookah.feature.tobacco.brand.model

import com.hookah.kek_hookah.feature.user.model.UserId

data class BrandForUpdate(
    val id: BrandId,
    val name: String,
    val description: String?,
    val updatedBy: UserId
)
