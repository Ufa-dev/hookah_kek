package com.hookah.kek_hookah.feature.tags.api

import com.hookah.kek_hookah.feature.auth.model.UserPrincipal
import com.hookah.kek_hookah.feature.tags.TagService
import com.hookah.kek_hookah.feature.tags.api.dto.TagNameForUpdateDto
import com.hookah.kek_hookah.feature.tags.model.Tag
import com.hookah.kek_hookah.feature.tags.model.TagForCreate
import com.hookah.kek_hookah.feature.tags.model.TagForUpdate
import com.hookah.kek_hookah.feature.tags.model.TagId
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@RequestMapping("/api/v1/tag")
class TagController(
    private val service: TagService
) {

    @PatchMapping("/{id}/name")
    suspend fun updateName(
        @PathVariable id: UUID,
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestBody @Validated request: TagNameForUpdateDto,
    ): ResponseEntity<Tag> {
        return TagForUpdate(
            tagId = TagId(id),
            name = request.name,
            userId = user.id
        ).let { request ->
            service.update(request)
        }.let { tag ->
            ResponseEntity.ok(tag)
        }
    }

    @PostMapping
    suspend fun create(
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestBody @Validated request: TagNameForUpdateDto,
    ): ResponseEntity<Tag> {
        return TagForCreate(
            name = request.name,
            userId = user.id
        ).let { request ->
            service.create(request)
        }.let { tag ->
            ResponseEntity.ok(tag)
        }
    }

    @GetMapping("/id/{id}")
    suspend fun findById(
        @PathVariable id: UUID
    ): ResponseEntity<Tag> {
        return service.findById(TagId(id))?.let { tag ->
            ResponseEntity.ok(tag)
        } ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/name/{name}")
    suspend fun findByName(
        @PathVariable name: String
    ): ResponseEntity<Tag> {
        return service.findByName(name)?.let { tag ->
            ResponseEntity.ok(tag)
        } ?: ResponseEntity.notFound().build()
    }

}