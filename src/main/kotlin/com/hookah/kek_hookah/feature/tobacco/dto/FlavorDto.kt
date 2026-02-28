package com.hookah.kek_hookah.feature.tobacco.dto

import java.util.*

data class FlavorDto(
    val id: UUID?,
    val brandId: UUID,
    val name: String,
    val strength: String? = null,
    val category: String? = null,
    val description: String? = null,
    val isActive: Boolean = true,
    val isFeatured: Boolean = true
)