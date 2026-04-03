package com.hookah.kek_hookah.feature.market.model

import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.user.model.UserId
import java.time.OffsetDateTime

data class MarketArc(
    val id: MarketArcId,
    val brandId: BrandId,
    val flavorId: FlavorId,
    val name: String,
    val weightGrams: Int,
    val count: Int,
    val gtin: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val updatedBy: UserId,
)
