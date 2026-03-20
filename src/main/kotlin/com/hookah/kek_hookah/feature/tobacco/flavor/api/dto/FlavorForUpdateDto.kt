package com.hookah.kek_hookah.feature.tobacco.flavor.api.dto

import jakarta.validation.constraints.*
import java.util.UUID

data class FlavorUpdateDto(
    @NotNull
    val brandId: UUID,

    @NotEmpty
    val name: String,

    val description: String? = null,

    @NotNull()
    @Min(0)
    @Max(10)
    val strength: Short
)