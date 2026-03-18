package com.hookah.kek_hookah.feature.tobacco.pack.model

import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.user.model.UserId

data class FlavorPackForCreate(
    val id: PackId,                // метка
    val flavorId: FlavorId,
    val currentWeightGrams: Long,
    val totalWeightGrams: Long,
    val userId: UserId
)