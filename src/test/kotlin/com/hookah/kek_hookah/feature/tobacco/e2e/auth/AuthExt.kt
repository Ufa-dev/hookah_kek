package com.hookah.kek_hookah.feature.tobacco.e2e.auth

import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

/**
 * A thin wrapper around [WebTestClient] that carries a pre-set Bearer token.
 * Delegates HTTP method calls to the underlying authorized client.
 */
class AuthorizedWebTestClient(private val delegate: WebTestClient) {

    fun get(): WebTestClient.RequestHeadersUriSpec<*> = delegate.get()
    fun post(): WebTestClient.RequestBodyUriSpec = delegate.post()
    fun put(): WebTestClient.RequestBodyUriSpec = delegate.put()
    fun patch(): WebTestClient.RequestBodyUriSpec = delegate.patch()
    fun delete(): WebTestClient.RequestHeadersUriSpec<*> = delegate.delete()
}

/**
 * Registers a fresh random user, logs in, and returns an [AuthorizedWebTestClient]
 * pre-configured with the resulting Bearer token.
 */
fun WebTestClient.randomUser(): AuthorizedWebTestClient {
    val uid = UUID.randomUUID().toString().take(8)
    val email = "test-$uid@example.com"
    val password = "Pass123!"

    post().uri("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(mapOf("email" to email, "name" to "Test $uid", "password" to password))
        .exchange()
        .expectStatus().is2xxSuccessful

    data class TokenResponse(val accessToken: String)

    val token = post().uri("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(mapOf("email" to email, "password" to password))
        .exchange()
        .expectStatus().isOk
        .expectBody<TokenResponse>()
        .returnResult().responseBody!!
        .accessToken

    val authorized = mutate()
        .defaultHeader("Authorization", "Bearer $token")
        .build()

    return AuthorizedWebTestClient(authorized)
}
