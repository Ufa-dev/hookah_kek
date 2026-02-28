package com.hookah.kek_hookah.feature.tobacco.mapper

import com.hookah.kek_hookah.feature.tobacco.dto.*
import com.hookah.kek_hookah.feature.tobacco.entity.*
import org.springframework.stereotype.Component

@Component
class TobaccoMapper {

    fun toBrandDto(entity: TobaccoBrand) = BrandDto(
        id = entity.id,
        name = entity.name,
        isActive = entity.isActive
    )

    fun toFlavorDto(entity: TobaccoFlavor) = FlavorDto(
        id = entity.id,
        brandId = entity.brand.id,
        name = entity.name,
        strength = entity.strength,
        category = entity.category,
        description = entity.description,
        isActive = entity.isActive,
        isFeatured = entity.isFeatured
    )

    fun toSkuDto(entity: TobaccoSku) = SkuDto(
        id = entity.id,
        flavorId = entity.flavor.id,
        weightGrams = entity.weightGrams,
        isActive = entity.isActive
    )
}