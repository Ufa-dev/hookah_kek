package com.hookah.kek_hookah.feature.tobacco.pack.model

import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.user.model.UserId
import org.bouncycastle.util.Pack
import java.time.OffsetDateTime

data class FlavorPack(
    val id: Pack,
    val tagId: Long,
    val flavorId: FlavorId,
    val currentWeightGrams: Long,
    val totalWeightGrams: Long,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val updatedBy: UserId
)
