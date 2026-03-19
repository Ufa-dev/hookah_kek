package com.hookah.kek_hookah.feature.tobacco.brand.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class BrandForCreateDto(
    @NotBlank
    @Size(min = 2, max = 64)
    val name: String,
    @NotBlank
    val description: String?,
)