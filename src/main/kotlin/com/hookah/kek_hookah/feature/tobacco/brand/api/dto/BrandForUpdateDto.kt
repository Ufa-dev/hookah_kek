package com.hookah.kek_hookah.feature.tobacco.brand.api.dto

import jakarta.validation.constraints.NotBlank

data class BrandForUpdateDto(
    @field:NotBlank
    val name: String,
    val description: String?
)
