package com.hookah.kek_hookah.feature.tobacco.repository

import com.hookah.kek_hookah.feature.tobacco.entity.TobaccoFlavor
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface TobaccoFlavorRepository : JpaRepository<TobaccoFlavor, UUID> {
    fun findAllByBrandId(brandId: UUID): List<TobaccoFlavor>
}