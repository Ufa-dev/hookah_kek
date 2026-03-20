package com.hookah.kek_hookah.feature.tobacco.pack.model

import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.user.model.UserId

data class PackForUpdate(
    val id: PackId,
    val name: String,
    val flavorId: FlavorId?,
    val currentWeightGrams: Int,
    val totalWeightGrams: Int,
    val updatedBy: UserId,
)
