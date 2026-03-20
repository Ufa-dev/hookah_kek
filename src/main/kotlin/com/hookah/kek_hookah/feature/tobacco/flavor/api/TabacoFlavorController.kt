package com.hookah.kek_hookah.feature.tobacco.flavor.api

import com.hookah.kek_hookah.feature.auth.model.UserPrincipal
import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.flavor.FlavorService
import com.hookah.kek_hookah.feature.tobacco.flavor.api.dto.FlavorCreateDto
import com.hookah.kek_hookah.feature.tobacco.flavor.api.dto.FlavorUpdateDto
import com.hookah.kek_hookah.feature.tobacco.flavor.api.dto.UpdateTagForFlavorDto
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorForCreate
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorForUpdate
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.TabacoFlavor
import com.hookah.kek_hookah.feature.tobacco.flavor.model.UpdateTagForFlavor
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/flavor")
class TabacoFlavorController(
    private val service: FlavorService
) {

    @PostMapping
    suspend fun create(
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestBody @Validated request: FlavorCreateDto
    ): ResponseEntity<TabacoFlavor> {
        val command = FlavorForCreate(
            brandId = BrandId(request.brandId),
            name = request.name,
            description = request.description,
            strength = request.strength,
            userId = user.id
        )
        return service.create(command)
            .let { ResponseEntity.ok(it) }
    }

    @PutMapping("/{id}")
    suspend fun update(
        @PathVariable id: UUID,
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestBody @Validated request: FlavorUpdateDto
    ): ResponseEntity<TabacoFlavor> {
        val command = FlavorForUpdate(
            flavorId = FlavorId(id),
            brandId = BrandId(request.brandId),
            name = request.name,
            description = request.description,
            strength = request.strength,
            userId = user.id
        )
        return service.update(command)
            .let { ResponseEntity.ok(it) }
    }

    @PatchMapping("/add-tag")
    suspend fun addTag(
        @RequestBody @Validated request: UpdateTagForFlavorDto
    ): ResponseEntity<TabacoFlavor> {
        return service.addTag(
            UpdateTagForFlavor(
                flavorId = request.flavorId,
                tagId = request.tagId
            )
        ).let { ResponseEntity.ok(it) }
    }

    @PatchMapping("/remove-tag")
    suspend fun removeTag(
        @RequestBody @Validated request: UpdateTagForFlavorDto
    ): ResponseEntity<TabacoFlavor> {
        return service.deleteTag(
            UpdateTagForFlavor(
                flavorId = request.flavorId,
                tagId = request.tagId
            )
        ).let { ResponseEntity.ok(it) }
    }

    @GetMapping("/flavors")
    suspend fun findByTags(
        @RequestParam("tags") tags: List<UUID>,
        @RequestParam(required = false) cursor: UUID?,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<TabacoFlavor>> {
        val limited = limit.coerceIn(1, 100) // ограничиваем максимальный лимит
        val flavors = service.findAllByTag(
            tags.map { TagId(it) },
            cursor?.let { FlavorId(it) },
            limited
        )
        val nextCursor = flavors.lastOrNull()?.id?.id?.toString() ?: ""
        return ResponseEntity.ok()
            .header("X-Next-Cursor", nextCursor)
            .body(flavors)
    }

    @GetMapping("/id/{id}")
    suspend fun findById(@PathVariable id: UUID): ResponseEntity<TabacoFlavor> {
        return service.findById(FlavorId(id))
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/name/{name}")
    suspend fun findByName(
        @PathVariable name: String,
        @RequestParam(required = false) cursor: UUID?,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<TabacoFlavor>> {
        val limited = limit.coerceIn(1, 100)
        val flavors = service.findAllByName(
            name,
            cursor?.let { FlavorId(it) },
            limited
        )
        val nextCursor = flavors.lastOrNull()?.id?.id?.toString() ?: ""
        return ResponseEntity.ok()
            .header("X-Next-Cursor", nextCursor)
            .body(flavors)
    }

    @GetMapping("/brand/{brandId}")
    suspend fun findByBrandId(
        @PathVariable brandId: UUID,
        @RequestParam(required = false) cursor: UUID?,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<TabacoFlavor>> {
        val limited = limit.coerceIn(1, 100)
        val flavors = service.findByBrandId(
            BrandId(brandId),
            cursor?.let { FlavorId(it) },
            limited
        )
        val nextCursor = flavors.lastOrNull()?.id?.id?.toString() ?: ""
        return ResponseEntity.ok()
            .header("X-Next-Cursor", nextCursor)
            .body(flavors)
    }
}