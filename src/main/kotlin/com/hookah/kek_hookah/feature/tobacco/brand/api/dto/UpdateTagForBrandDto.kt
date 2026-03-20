package com.hookah.kek_hookah.feature.tobacco.brand.api.dto

import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import jakarta.validation.constraints.NotNull

data class UpdateTagForBrandDto(
    @field:NotNull
    val tagId: TagId,
    @field:NotNull
    val brandId: BrandId
)
