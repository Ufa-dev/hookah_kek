package com.hookah.kek_hookah.feature.tobacco.repository

import com.hookah.kek_hookah.feature.tobacco.entity.TobaccoSku
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface TobaccoSkuRepository : JpaRepository<TobaccoSku, UUID> {
    fun findAllByFlavorId(flavorId: UUID): List<TobaccoSku>
}