package com.hookah.kek_hookah.feature.tobacco.dto

import java.util.*

data class BrandDto(
    val id: UUID?,
    val name: String,
    val isActive: Boolean = true
)