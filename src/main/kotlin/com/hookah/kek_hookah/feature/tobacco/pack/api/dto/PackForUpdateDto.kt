package com.hookah.kek_hookah.feature.tobacco.pack.api.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.*

data class PackForUpdateDto(
    @field:NotBlank
    val name: String,

    val flavorId: UUID?,

    @field:NotNull
    @field:Min(1)
    val totalWeightGrams: Int,

    @field:NotNull
    @field:Min(0)
    val currentWeightGrams: Int,
)
