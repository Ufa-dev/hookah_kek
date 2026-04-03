package com.hookah.kek_hookah.feature.market.model

import com.hookah.kek_hookah.feature.user.model.UserId

data class MarketForUpdateCount(
    val id: MarketArcId,
    val count: Int,
    val updatedBy: UserId,
)
