package com.hookah.kek_hookah.feature.tobacco.dto

import java.util.*

data class SkuDto(
    val id: UUID?,
    val flavorId: UUID,
    val weightGrams: Int,
    val isActive: Boolean = true
)