package com.hookah.kek_hookah.feature.market.api.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class MarketUpdateCountDto(
    @NotNull @Min(0) val count: Int,
)
