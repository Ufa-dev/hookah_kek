package com.hookah.kek_hookah.feature.tobacco.service

import com.hookah.kek_hookah.feature.tobacco.dto.*
import com.hookah.kek_hookah.feature.tobacco.entity.*
import com.hookah.kek_hookah.feature.tobacco.mapper.TobaccoMapper
import com.hookah.kek_hookah.feature.tobacco.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class TobaccoService(
    private val brandRepo: TobaccoBrandRepository,
    private val flavorRepo: TobaccoFlavorRepository,
    private val skuRepo: TobaccoSkuRepository,
    private val mapper: TobaccoMapper
) {

    // ----------------- BRANDS -----------------
    fun getAllBrands(): List<BrandDto> = brandRepo.findAll().map(mapper::toBrandDto)

    fun getBrand(id: UUID): BrandDto =
        brandRepo.findById(id).map(mapper::toBrandDto)
            .orElseThrow { RuntimeException("Brand not found") }

    fun createBrand(dto: BrandDto): BrandDto =
        mapper.toBrandDto(brandRepo.save(TobaccoBrand(name = dto.name, isActive = dto.isActive)))

    fun updateBrand(id: UUID, dto: BrandDto): BrandDto {
        val brand = brandRepo.findById(id).orElseThrow { RuntimeException("Brand not found") }
        val updated = brand.copy(name = dto.name, isActive = dto.isActive)
        return mapper.toBrandDto(brandRepo.save(updated))
    }

    fun deleteBrand(id: UUID) = brandRepo.deleteById(id)

    // ----------------- FLAVORS -----------------
    fun getAllFlavors(): List<FlavorDto> = flavorRepo.findAll().map(mapper::toFlavorDto)

    fun getFlavor(id: UUID): FlavorDto =
        flavorRepo.findById(id).map(mapper::toFlavorDto)
            .orElseThrow { RuntimeException("Flavor not found") }

    fun getFlavorsByBrand(brandId: UUID): List<FlavorDto> =
        flavorRepo.findAllByBrandId(brandId).map(mapper::toFlavorDto)

    fun createFlavor(dto: FlavorDto): FlavorDto {
        val brand = brandRepo.findById(dto.brandId).orElseThrow { RuntimeException("Brand not found") }
        val flavor = TobaccoFlavor(
            brand = brand,
            name = dto.name,
            strength = dto.strength,
            category = dto.category,
            description = dto.description,
            isActive = dto.isActive,
            isFeatured = dto.isFeatured
        )
        return mapper.toFlavorDto(flavorRepo.save(flavor))
    }

    fun updateFlavor(id: UUID, dto: FlavorDto): FlavorDto {
        val flavor = flavorRepo.findById(id).orElseThrow { RuntimeException("Flavor not found") }
        val brand = brandRepo.findById(dto.brandId).orElseThrow { RuntimeException("Brand not found") }
        val updated = flavor.copy(
            brand = brand,
            name = dto.name,
            strength = dto.strength,
            category = dto.category,
            description = dto.description,
            isActive = dto.isActive,
            isFeatured = dto.isFeatured
        )
        return mapper.toFlavorDto(flavorRepo.save(updated))
    }

    fun deleteFlavor(id: UUID) = flavorRepo.deleteById(id)

    // ----------------- SKUS -----------------
    fun getAllSkus(): List<SkuDto> = skuRepo.findAll().map(mapper::toSkuDto)

    fun getSku(id: UUID): SkuDto =
        skuRepo.findById(id).map(mapper::toSkuDto)
            .orElseThrow { RuntimeException("SKU not found") }

    fun getSkusByFlavor(flavorId: UUID): List<SkuDto> =
        skuRepo.findAllByFlavorId(flavorId).map(mapper::toSkuDto)

    fun createSku(dto: SkuDto): SkuDto {
        val flavor = flavorRepo.findById(dto.flavorId).orElseThrow { RuntimeException("Flavor not found") }
        val sku = TobaccoSku(flavor = flavor, weightGrams = dto.weightGrams, isActive = dto.isActive)
        return mapper.toSkuDto(skuRepo.save(sku))
    }

    fun updateSku(id: UUID, dto: SkuDto): SkuDto {
        val sku = skuRepo.findById(id).orElseThrow { RuntimeException("SKU not found") }
        val flavor = flavorRepo.findById(dto.flavorId).orElseThrow { RuntimeException("Flavor not found") }
        val updated = sku.copy(flavor = flavor, weightGrams = dto.weightGrams, isActive = dto.isActive)
        return mapper.toSkuDto(skuRepo.save(updated))
    }

    fun deleteSku(id: UUID) = skuRepo.deleteById(id)
}