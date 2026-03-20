package com.hookah.kek_hookah.infrastructure.config.security

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebExchange
import java.time.OffsetDateTime


@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException::class)
    suspend fun handleBadCredentials(
        ex: BadCredentialsException,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ErrorResponse(
                status = 401,
                error = "Unauthorized",
                message = ex.message ?: "Invalid credentials",
                path = exchange.request.path.value()
            )
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    suspend fun handleIllegalArgument(
        ex: IllegalArgumentException,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                status = 400,
                error = "Bad Request",
                message = ex.message ?: "Invalid request",
                path = exchange.request.path.value()
            )
        )
    }

    @ExceptionHandler(NoSuchElementException::class)
    suspend fun handleNotFound(
        ex: NoSuchElementException,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(
                status = 404,
                error = "Not Found",
                message = ex.message ?: "Resource not found",
                path = exchange.request.path.value()
            )
        )
    }
}

data class ErrorResponse(
    val timestamp: OffsetDateTime = OffsetDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null
)
