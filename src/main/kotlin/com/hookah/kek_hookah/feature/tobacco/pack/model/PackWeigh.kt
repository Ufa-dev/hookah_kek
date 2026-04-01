package com.hookah.kek_hookah.feature.tobacco.pack.model

import com.hookah.kek_hookah.feature.user.model.UserId
import java.time.OffsetDateTime

data class PackWeigh(
    val tagId: PackTagId,
    val currentWeightGrams: Int,
    val updatedAt: OffsetDateTime,
    val updatedBy: UserId,
)