package com.hookah.kek_hookah.feature.tobacco.flavor.model

import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.user.model.UserId

data class FlavorForUpdate(
    val flavorId: FlavorId,
    val brandId: BrandId,
    val name: String,
    val description: String,
    val warehouseProductId : String,
    val strength: Int,
    val userId: UserId
)