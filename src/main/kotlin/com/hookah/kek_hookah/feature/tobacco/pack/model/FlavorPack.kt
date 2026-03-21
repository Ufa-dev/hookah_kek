package com.hookah.kek_hookah.feature.tobacco.pack.model

import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.user.model.UserId
import java.time.OffsetDateTime

data class FlavorPack(
    val id: PackId,
    val tagId: PackTagId,
    val name: String,
    val flavorId: FlavorId?,
    val currentWeightGrams: Int,
    val totalWeightGrams: Int,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val updatedBy: UserId,
)
