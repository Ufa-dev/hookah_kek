package com.hookah.kek_hookah.feature.market.api.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class MarketCreateDto(
    @NotNull val brandId: UUID,
    @NotNull val flavorId: UUID,
    @NotBlank val name: String,
    @NotNull @Min(1) val weightGrams: Int,
    @Min(0) val count: Int = 0,
    val gtin: String? = null,
)
