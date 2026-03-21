package com.hookah.kek_hookah.feature.tobacco.pack.api

import com.hookah.kek_hookah.feature.auth.model.UserPrincipal
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.tobacco.pack.PackService
import com.hookah.kek_hookah.feature.tobacco.pack.api.dto.PackForCreateDto
import com.hookah.kek_hookah.feature.tobacco.pack.api.dto.PackForUpdateDto
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackForCreate
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackForUpdate
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackId
import com.hookah.kek_hookah.utils.crud.Slice
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/pack")
class PackController(
    private val service: PackService,
) {

    @GetMapping
    suspend fun list(
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(required = false) after: String?,
    ): ResponseEntity<Slice<FlavorPack>> {
        val afterId = after?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        return service.list(limit.coerceIn(1, 100), afterId)
            .let { ResponseEntity.ok(it) }
    }

    @GetMapping("/{id}")
    suspend fun findById(
        @PathVariable id: String,
    ): ResponseEntity<FlavorPack> =
        service.findById(PackId(id))
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @PostMapping
    suspend fun create(
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestBody @Validated request: PackForCreateDto,
    ): ResponseEntity<FlavorPack> =
        PackForCreate(
            tagId = request.tagId,
            name = request.name,
            flavorId = request.flavorId?.let { FlavorId(it) },
            currentWeightGrams = request.currentWeightGrams,
            totalWeightGrams = request.totalWeightGrams,
            updatedBy = user.id,
        ).let { service.create(it) }
            .let { ResponseEntity.ok(it) }

    @PutMapping("/{id}")
    suspend fun update(
        @PathVariable id: String,
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestBody @Validated request: PackForUpdateDto,
    ): ResponseEntity<FlavorPack> =
        PackForUpdate(
            id = PackId(id),
            name = request.name,
            flavorId = request.flavorId?.let { FlavorId(it) },
            currentWeightGrams = request.currentWeightGrams,
            totalWeightGrams = request.totalWeightGrams,
            updatedBy = user.id,
        ).let { service.update(it) }
            .let { ResponseEntity.ok(it) }

    @DeleteMapping("/{id}")
    suspend fun delete(
        @PathVariable id: String,
    ): ResponseEntity<Void> {
        service.delete(PackId(id))
        return ResponseEntity.noContent().build()
    }
}
