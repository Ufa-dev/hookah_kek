package com.hookah.kek_hookah.feature.user.api

import com.hookah.kek_hookah.feature.auth.model.UserPrincipal
import com.hookah.kek_hookah.feature.user.UserService
import com.hookah.kek_hookah.feature.user.api.dto.UserForUpdateDto
import com.hookah.kek_hookah.feature.user.model.User
import com.hookah.kek_hookah.feature.user.model.UserForUpdate
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/users/me")
class UserController(
    private val service: UserService
) {

    @PatchMapping
    suspend fun updateMe(
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestBody @Validated request: UserForUpdateDto
    ): ResponseEntity<User> {
        return UserForUpdate(
            userId = user.id,
            name = request.name,
            email = request.email
        ).let { request ->
            service.update(request)
        }.let { updated ->
            ResponseEntity.ok(updated)
        }
    }

    @GetMapping
    suspend fun getMe(
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<User> {
        return service.findById(user.id)?.let { user ->
            ResponseEntity.ok(user)
        } ?: ResponseEntity.notFound().build()
    }
}
