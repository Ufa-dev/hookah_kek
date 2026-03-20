package com.hookah.kek_hookah.feature.tobacco.flavor.model

import com.hookah.kek_hookah.feature.tags.model.TagId

data class UpdateTagForFlavor (
    val tagId: TagId,
    val flavorId: FlavorId
)