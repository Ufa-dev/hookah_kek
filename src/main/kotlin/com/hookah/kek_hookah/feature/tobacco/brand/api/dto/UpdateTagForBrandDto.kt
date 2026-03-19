package com.hookah.kek_hookah.feature.tobacco.brand.api.dto

import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import jakarta.validation.constraints.NotBlank

data class UpdateTagForBrandDto(
    @field:NotBlank(message = "TagId cannot be blank")
    val tagId: TagId,
    @field:NotBlank(message = "BrandId cannot be blank")
    val brandId: BrandId
)
