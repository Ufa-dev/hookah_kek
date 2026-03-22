package com.hookah.kek_hookah.feature.tobacco.e2e.auth


import com.hookah.kek_hookah.feature.auth.api.dto.AuthResponse
import com.hookah.kek_hookah.feature.auth.api.dto.CreditsToLogin
import com.hookah.kek_hookah.feature.auth.api.dto.RegisterRequest
import com.hookah.kek_hookah.feature.auth.api.dto.TokenToRefresh
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

const val AUTH_URL = "/api/v1/auth"

class AuthorizedWebTestClient(
    val userEmail: String,
    val userName: String,
    private val webTestClient: WebTestClient
) : WebTestClient by webTestClient

fun WebTestClient.randomUser(): AuthorizedWebTestClient {
    val salt = UUID.randomUUID().toString()
    val email = "test-${salt}@test.com"
    val name = "Test User #$salt"

    val password = "testpass$salt".take(12)

    val accessToken = registerTestUser(
        email = email,
        name = name,
        password = password
    ).accessToken

    return mutate()
        .defaultHeader("Authorization", "Bearer $accessToken")
        .build().let { client ->
            AuthorizedWebTestClient(userEmail = email, userName = name, webTestClient = client)
        }
}

fun WebTestClient.registerTestUser(
    email: String,
    name: String,
    password: String,
): AuthResponse {
    require(password.length in 6..32) { "Password must be 6-32 characters, got: ${password.length}" }

    return post()
        .uri("$AUTH_URL/register")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(RegisterRequest(email = email, name = name, password = password))
        .exchange()
        .expectStatus().isCreated
        .expectBody<AuthResponse>()
        .returnResult()
        .responseBody!!
}

fun WebTestClient.register(
    email: String,
    name: String,
    password: String
): WebTestClient.ResponseSpec {
    require(password.length in 6..32) { "Password must be 6-32 characters, got: ${password.length}" }

    return post()
        .uri("$AUTH_URL/register")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(RegisterRequest(email = email, name = name, password = password))
        .exchange()
}

fun WebTestClient.login(
    email: String,
    password: String
): WebTestClient.ResponseSpec {
    require(password.length in 6..32) { "Password must be 6-32 characters, got: ${password.length}" }

    return post()
        .uri("$AUTH_URL/login")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(CreditsToLogin(email = email, password = password))
        .exchange()
}

fun WebTestClient.refreshToken(token: String): WebTestClient.ResponseSpec {
    return post()
        .uri("$AUTH_URL/refresh")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(TokenToRefresh(token = token))
        .exchange()
}
