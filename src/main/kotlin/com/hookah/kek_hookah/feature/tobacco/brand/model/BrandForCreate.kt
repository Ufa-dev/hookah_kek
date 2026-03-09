package com.hookah.kek_hookah.feature.tobacco.brand.model

import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.feature.user.model.UserId


data class BrandForCreate(
    val name: String,
    val description: String?,
    val tags: List<TagId>,
    val updatedBy: UserId
)