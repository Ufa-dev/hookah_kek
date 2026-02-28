package com.hookah.kek_hookah.feature.tobacco.controller

import com.hookah.kek_hookah.feature.tobacco.dto.*
import com.hookah.kek_hookah.feature.tobacco.service.TobaccoService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/tobacco")
class TobaccoController(
    private val service: TobaccoService
) {

    // -------- BRANDS --------
    @GetMapping("/brands")
    fun getAllBrands() = service.getAllBrands()

    @GetMapping("/brands/{id}")
    fun getBrand(@PathVariable id: UUID) = service.getBrand(id)

    @PostMapping("/brands")
    fun createBrand(@RequestBody dto: BrandDto) = service.createBrand(dto)

    @PutMapping("/brands/{id}")
    fun updateBrand(@PathVariable id: UUID, @RequestBody dto: BrandDto) = service.updateBrand(id, dto)

    @DeleteMapping("/brands/{id}")
    fun deleteBrand(@PathVariable id: UUID) = service.deleteBrand(id)

    // -------- FLAVORS --------
    @GetMapping("/flavors")
    fun getAllFlavors() = service.getAllFlavors()

    @GetMapping("/flavors/{id}")
    fun getFlavor(@PathVariable id: UUID) = service.getFlavor(id)

    @GetMapping("/brands/{brandId}/flavors")
    fun getFlavorsByBrand(@PathVariable brandId: UUID) = service.getFlavorsByBrand(brandId)

    @PostMapping("/flavors")
    fun createFlavor(@RequestBody dto: FlavorDto) = service.createFlavor(dto)

    @PutMapping("/flavors/{id}")
    fun updateFlavor(@PathVariable id: UUID, @RequestBody dto: FlavorDto) = service.updateFlavor(id, dto)

    @DeleteMapping("/flavors/{id}")
    fun deleteFlavor(@PathVariable id: UUID) = service.deleteFlavor(id)

    // -------- SKUS --------
    @GetMapping("/skus")
    fun getAllSkus() = service.getAllSkus()

    @GetMapping("/skus/{id}")
    fun getSku(@PathVariable id: UUID) = service.getSku(id)

    @GetMapping("/flavors/{flavorId}/skus")
    fun getSkusByFlavor(@PathVariable flavorId: UUID) = service.getSkusByFlavor(flavorId)

    @PostMapping("/skus")
    fun createSku(@RequestBody dto: SkuDto) = service.createSku(dto)

    @PutMapping("/skus/{id}")
    fun updateSku(@PathVariable id: UUID, @RequestBody dto: SkuDto) = service.updateSku(id, dto)

    @DeleteMapping("/skus/{id}")
    fun deleteSku(@PathVariable id: UUID) = service.deleteSku(id)
}