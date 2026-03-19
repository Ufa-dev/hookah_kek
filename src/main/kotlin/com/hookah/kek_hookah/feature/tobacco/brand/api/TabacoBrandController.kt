package com.hookah.kek_hookah.feature.tobacco.brand.api

import com.hookah.kek_hookah.feature.auth.model.UserPrincipal
import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.feature.tobacco.brand.BrandService
import com.hookah.kek_hookah.feature.tobacco.brand.api.dto.BrandForCreateDto
import com.hookah.kek_hookah.feature.tobacco.brand.api.dto.BrandForUpdateDto
import com.hookah.kek_hookah.feature.tobacco.brand.api.dto.UpdateTagForBrandDto
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandForCreate
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandForUpdate
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import com.hookah.kek_hookah.feature.tobacco.brand.model.UpdateTagForBrand
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/brand")
class TabacoBrandController(
    private val service: BrandService
) {

    @PutMapping("/{id}")
    suspend fun update(
        @PathVariable id: UUID,
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestBody @Validated request: BrandForUpdateDto,
    ): ResponseEntity<TabacoBrand> {
        return BrandForUpdate(
            id = BrandId(id),
            name = request.name,
            description = request.description,
            updatedBy = user.id
        ).let { service.update(it) }
            .let { brand -> ResponseEntity.ok(brand) }
    }

    @PostMapping
    suspend fun create(
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestBody @Validated request: BrandForCreateDto,
    ): ResponseEntity<TabacoBrand> {
        return BrandForCreate(
            name = request.name,
            description = request.description,
            updatedBy = user.id
        ).let { request ->
            service.create(request)
        }.let { brand ->
            ResponseEntity.ok(brand)
        }
    }

    @PatchMapping("/add-tag")
    suspend fun addTag(
        @RequestBody @Validated request: UpdateTagForBrandDto
    ): ResponseEntity<TabacoBrand> {
        return service.addTag(
            UpdateTagForBrand(
                brandId = request.brandId,
                tagId = request.tagId
            )
        ).let { ResponseEntity.ok(it) }
    }

    @PatchMapping("/remove-tag")
    suspend fun removeTag(
        @RequestBody @Validated request: UpdateTagForBrandDto
    ): ResponseEntity<TabacoBrand> {
        return service.deleteTag(
            UpdateTagForBrand(
                brandId = request.brandId,
                tagId = request.tagId
            )
        ).let { ResponseEntity.ok(it) }
    }

    @GetMapping("/brands")
    suspend fun findByTags(
        @RequestParam("tags") tags: List<UUID>
    ): ResponseEntity<List<TabacoBrand>> {
        return service.findAllByTag(tags.map { TagId(it) }).let { brands ->
            ResponseEntity.ok(brands)
        }
    }

    @GetMapping("/id/{id}")
    suspend fun findById(
        @PathVariable id: UUID
    ): ResponseEntity<TabacoBrand> {
        return service.findById(BrandId(id))?.let { brand ->
            ResponseEntity.ok(brand)
        } ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/name/{name}")
    suspend fun findByName(
        @PathVariable name: String
    ): ResponseEntity<List<TabacoBrand>> {
        return service.findAllByName(name).let { brand ->
            ResponseEntity.ok(brand)
        }
    }
}