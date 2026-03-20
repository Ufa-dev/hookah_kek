package com.hookah.kek_hookah.feature.market.model

import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.user.model.UserId

data class MarketForCreate(
    val brandId: BrandId,
    val flavorId: FlavorId,
    val name: String,
    val weightGrams: Int,
    val gtin: String?,
    val updatedBy: UserId,
)
