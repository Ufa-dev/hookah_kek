package com.hookah.kek_hookah.feature.tobacco.brand.model

import com.hookah.kek_hookah.feature.tags.model.TagId

data class AddTagForBrand(
    val tagId: TagId,
    val brandId: BrandId
)
