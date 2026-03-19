package com.hookah.kek_hookah.feature.tobacco.brand.api.dto

import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import jakarta.validation.constraints.NotEmpty
import org.jetbrains.annotations.NotNull

data class BrandForUpdateDto(
    @NotNull
    val id: BrandId,
    @NotEmpty
    val name: String,
    @NotEmpty
    val description: String
)
