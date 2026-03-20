package com.hookah.kek_hookah.feature.tobacco.flavor.api.dto

import java.util.UUID

data class FlavorSearchDto(
    val id: UUID,
    val name: String,
    val brandId: UUID,
    val brandName: String,
)
