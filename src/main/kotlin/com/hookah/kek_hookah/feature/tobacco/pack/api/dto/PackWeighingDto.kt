package com.hookah.kek_hookah.feature.tobacco.pack.api.dto

import java.time.OffsetDateTime

data class PackWeighingDto(
    val tagId: String,
    val currentWeightGrams: Int,
    val updatedAt: OffsetDateTime,
)
