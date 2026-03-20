package com.hookah.kek_hookah.feature.tobacco.flavor.model

import com.hookah.kek_hookah.feature.tags.model.Tag
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.user.model.UserId
import java.time.OffsetDateTime

data class TabacoFlavor(
    val id: FlavorId,
    val brandId: BrandId,
    val name: String,
    val description: String?,
    val tags: List<Tag> = emptyList(),
   // val warehouseProductId: String,
    val strength: Short?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val updatedBy: UserId
)
