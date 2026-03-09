package com.hookah.kek_hookah.feature.tags.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class TagNameForUpdateDto(
    @NotBlank
    @Size(min = 3, max = 32)
    val name: String
)