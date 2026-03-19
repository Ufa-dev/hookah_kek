package com.hookah.kek_hookah.feature.tobacco.brand.model

import com.hookah.kek_hookah.feature.tags.model.Tag
import com.hookah.kek_hookah.feature.user.model.UserId
import java.time.OffsetDateTime

data class TabacoBrand(
    val id: BrandId,
    val name: String,
    val description: String?,
    val tags: List<Tag> = emptyList(),
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val updatedBy: UserId
)
