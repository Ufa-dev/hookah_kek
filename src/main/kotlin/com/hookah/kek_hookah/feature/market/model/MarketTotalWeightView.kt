package com.hookah.kek_hookah.feature.market.model

import java.util.UUID

data class MarketTotalWeightView(
    val flavorId: UUID,
    val totalWeightGrams: Long,
)
