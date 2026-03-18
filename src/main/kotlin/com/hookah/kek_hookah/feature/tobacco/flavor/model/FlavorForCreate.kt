package com.hookah.kek_hookah.feature.tobacco.flavor.model

import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.user.model.UserId

data class FlavorForCreate(
    val brandId: BrandId,
    val name: String,
    val description: String,
    val strength: Int,
    val warehouseProductId: String,
    val userId: UserId
)