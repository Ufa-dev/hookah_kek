package com.hookah.kek_hookah.feature.tobacco.flavor.api

import com.hookah.kek_hookah.feature.tobacco.flavor.FlavorService
import com.hookah.kek_hookah.feature.tobacco.flavor.api.dto.FlavorSearchDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/flavor")
class FlavorController(
    private val service: FlavorService,
) {

    @GetMapping("/search")
    suspend fun search(
        @RequestParam(defaultValue = "") q: String,
        @RequestParam(defaultValue = "10") limit: Int,
    ): ResponseEntity<List<FlavorSearchDto>> =
        service.search(q, limit.coerceIn(1, 50))
            .let { ResponseEntity.ok(it) }

    @GetMapping("/{id}")
    suspend fun findById(
        @PathVariable id: String,
    ): ResponseEntity<FlavorSearchDto> =
        service.findById(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
}
