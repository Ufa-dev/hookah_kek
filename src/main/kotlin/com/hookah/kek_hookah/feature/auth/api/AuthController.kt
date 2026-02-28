package com.hookah.kek_hookah.feature.auth.api

import com.hookah.kek_hookah.feature.auth.AuthService
import com.hookah.kek_hookah.feature.auth.api.dto.AuthResponse
import com.hookah.kek_hookah.feature.auth.api.dto.CreditsToLogin
import com.hookah.kek_hookah.feature.auth.api.dto.RegisterRequest
import com.hookah.kek_hookah.feature.auth.api.dto.TokenToRefresh
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/register")
    suspend fun register(
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<AuthResponse> {
        val response = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/login")
    suspend fun login(
        @RequestBody request: CreditsToLogin
    ): ResponseEntity<AuthResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    suspend fun refreshAccessToken(
        @RequestBody request: TokenToRefresh
    ): ResponseEntity<AuthResponse> {
        val response = authService.refreshAccessToken(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/logout")
    suspend fun logout(
        @RequestBody request: TokenToRefresh
    ): ResponseEntity<Unit> {
        authService.logout(request)
        return ResponseEntity.noContent().build()
    }

}
