package com.hookah.kek_hookah.feature.tobacco.flavor.api.dto

import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import jakarta.validation.constraints.NotNull

data class UpdateTagForFlavorDto(
    @NotNull
    val flavorId: FlavorId,

    @NotNull
    val tagId: TagId
)