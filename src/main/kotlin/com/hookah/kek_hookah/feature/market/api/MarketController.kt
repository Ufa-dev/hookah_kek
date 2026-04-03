package com.hookah.kek_hookah.feature.market.api

import com.hookah.kek_hookah.feature.auth.model.UserPrincipal
import com.hookah.kek_hookah.feature.market.MarketService
import com.hookah.kek_hookah.feature.market.api.dto.MarketCreateDto
import com.hookah.kek_hookah.feature.market.api.dto.MarketUpdateCountDto
import com.hookah.kek_hookah.feature.market.api.dto.MarketUpdateDto
import com.hookah.kek_hookah.feature.market.model.MarketArcId
import com.hookah.kek_hookah.feature.market.model.MarketArcView
import com.hookah.kek_hookah.feature.market.model.MarketForCreate
import com.hookah.kek_hookah.feature.market.model.MarketForUpdate
import com.hookah.kek_hookah.feature.market.model.MarketForUpdateCount
import com.hookah.kek_hookah.feature.market.model.MarketTotalWeightView
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/market")
class MarketController(
    private val service: MarketService
) {

    @PostMapping
    suspend fun create(
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestBody @Validated body: MarketCreateDto
    ): ResponseEntity<MarketArcView> {
        val result = service.create(
            MarketForCreate(
                brandId = BrandId(body.brandId),
                flavorId = FlavorId(body.flavorId),
                name = body.name,
                weightGrams = body.weightGrams,
                count = body.count,
                gtin = body.gtin,
                updatedBy = user.id,
            )
        )
        return ResponseEntity.ok(result)
    }

    @PutMapping("/{id}")
    suspend fun update(
        @PathVariable id: UUID,
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestBody @Validated body: MarketUpdateDto
    ): ResponseEntity<MarketArcView> {
        val result = service.update(
            MarketForUpdate(
                id = MarketArcId(id),
                brandId = BrandId(body.brandId),
                flavorId = FlavorId(body.flavorId),
                name = body.name,
                weightGrams = body.weightGrams,
                count = body.count,
                gtin = body.gtin,
                updatedBy = user.id,
            )
        )
        return ResponseEntity.ok(result)
    }

    @PatchMapping("/{id}/count")
    suspend fun updateCount(
        @PathVariable id: UUID,
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestBody @Validated body: MarketUpdateCountDto
    ): ResponseEntity<MarketArcView> {
        val result = service.updateCount(
            MarketForUpdateCount(
                id = MarketArcId(id),
                count = body.count,
                updatedBy = user.id,
            )
        )
        return ResponseEntity.ok(result)
    }

    @GetMapping("/total-weight/{flavorId}")
    suspend fun totalWeightByFlavor(
        @PathVariable flavorId: UUID
    ): ResponseEntity<MarketTotalWeightView> =
        ResponseEntity.ok(service.totalWeightByFlavor(flavorId))

    @DeleteMapping("/{id}")
    suspend fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        service.delete(MarketArcId(id))
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}")
    suspend fun findById(@PathVariable id: UUID): ResponseEntity<MarketArcView> =
        service.findById(MarketArcId(id))
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @GetMapping
    suspend fun list(
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(required = false) after: UUID?,
        @RequestParam(required = false) brandName: String?,
        @RequestParam(required = false) flavorName: String?,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) weightMin: Int?,
        @RequestParam(required = false) weightMax: Int?,
        @RequestParam(required = false) countMin: Int?,
        @RequestParam(defaultValue = "updated_at") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDir: String,
    ): ResponseEntity<List<MarketArcView>> {
        val limited = limit.coerceIn(1, 100)
        val items = service.list(
            limit = limited,
            afterId = after,
            brandName = brandName,
            flavorName = flavorName,
            nameContains = name,
            weightMin = weightMin,
            weightMax = weightMax,
            countMin = countMin,
            sortBy = sortBy,
            sortDir = sortDir,
        )
        val nextCursor = items.lastOrNull()?.id?.id?.toString() ?: ""
        return ResponseEntity.ok()
            .header("X-Next-Cursor", nextCursor)
            .body(items)
    }
}
