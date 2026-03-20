package com.hookah.kek_hookah.feature.auth

import com.hookah.kek_hookah.feature.auth.api.dto.AuthResponse
import com.hookah.kek_hookah.feature.auth.api.dto.CreditsToLogin
import com.hookah.kek_hookah.feature.auth.api.dto.RegisterRequest
import com.hookah.kek_hookah.feature.auth.api.dto.TokenToRefresh
import com.hookah.kek_hookah.feature.auth.internal.repository.RefreshTokenRepository
import com.hookah.kek_hookah.feature.auth.internal.usecase.CreatePasswordCommand
import com.hookah.kek_hookah.feature.auth.internal.usecase.SaveRefreshTokenCommand
import com.hookah.kek_hookah.feature.auth.internal.usecase.VerifyLoginCommand
import com.hookah.kek_hookah.feature.user.UserService
import com.hookah.kek_hookah.feature.user.model.UserForCreate
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service


@Service
class AuthService(
    private val createPasswordCommand: CreatePasswordCommand,
    private val verifyLoginCommand: VerifyLoginCommand,
    private val saveRefreshTokenCommand: SaveRefreshTokenCommand,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtProvider: JwtProvider,
    private val userService: UserService,

    @Value($$"${app.security.jwt.access-token-expiration-ms:1800000}")
    private val accessTokenExpiration: Long,

    @Value($$"${app.security.jwt.refresh-token-expiration-ms:86400000}")
    private val refreshTokenExpiration: Long,
) {

    suspend fun register(request: RegisterRequest): AuthResponse {
        val user = UserForCreate(
            name = request.name,
            email = request.email
        ).let { request ->
            userService.create(request)
        }

        createPasswordCommand.execute(user.id, request.password)

        val accessToken = jwtProvider.generateAccessToken(user)
        val refreshToken = jwtProvider.generateRefreshToken(user)

        val jti = jwtProvider.extractJti(refreshToken)!!
        saveRefreshTokenCommand.execute(user.id, jti)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = accessTokenExpiration,
            refreshExpiresIn = refreshTokenExpiration
        )
    }

    suspend fun login(request: CreditsToLogin): AuthResponse {
        val user = verifyLoginCommand.execute(request.email, request.password)

        val accessToken = jwtProvider.generateAccessToken(user)
        val refreshToken = jwtProvider.generateRefreshToken(user)

        val jti = jwtProvider.extractJti(refreshToken)!!
        saveRefreshTokenCommand.execute(user.id, jti)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = accessTokenExpiration,
            refreshExpiresIn = refreshTokenExpiration
        )
    }

    suspend fun refreshAccessToken(request: TokenToRefresh): AuthResponse {
        val jti = jwtProvider.extractJti(request.token)
            ?: throw BadCredentialsException("Invalid refresh token")

        val storedToken = refreshTokenRepository.findByJti(jti)
            ?: throw BadCredentialsException("Refresh token has been revoked")

        val newAccessToken = jwtProvider.generateNewAccessToken(request.token)
            ?: throw BadCredentialsException("Failed to generate new access token")

        val newRefreshToken = jwtProvider.generateRefreshToken(
            userService.findById(storedToken.userId)!!
        )

        refreshTokenRepository.deleteByJti(jti)

        val newJti = jwtProvider.extractJti(newRefreshToken)!!
        saveRefreshTokenCommand.execute(storedToken.userId, newJti)

        return AuthResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            expiresIn = accessTokenExpiration,
            refreshExpiresIn = refreshTokenExpiration
        )
    }

    //todo maybe delete all userTokens?
    suspend fun logout(request: TokenToRefresh) {
        val jti = jwtProvider.extractJti(request.token)
            ?: throw BadCredentialsException("Invalid refresh token")

        val deleted = refreshTokenRepository.deleteByJti(jti)
        if (!deleted) {
            throw BadCredentialsException("Refresh token not found or already revoked")
        }
    }
}
