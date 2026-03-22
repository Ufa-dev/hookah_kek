package com.hookah.kek_hookah.feature.tobacco.e2e.auth


import com.hookah.kek_hookah.feature.auth.api.dto.AuthResponse
import com.hookah.kek_hookah.feature.auth.api.dto.TokenToRefresh
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

@IntegrationTest
class AuthRefreshTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should refresh access token with valid refresh token`() = runTest {
        // Arrange: register and login user
        val salt = UUID.randomUUID().toString()
        val authResponse = unauthorizedClient.registerTestUser(
            email = "refresh-$salt@example.com",
            name = "Refresh User",
            password = "password123"
        )

        // Act: refresh token
        val refreshRequest = TokenToRefresh(token = authResponse.refreshToken)

        val result = unauthorizedClient
            .post()
            .uri("${AUTH_URL}/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(refreshRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody<AuthResponse>()
            .returnResult()
            .responseBody!!

        // Assert
        assertAll(
            { assertNotNull(result.accessToken) },
            { assertNotNull(result.refreshToken) },
            { assertTrue(result.expiresIn > 0) },
            { assertTrue(result.refreshExpiresIn > 0) }
        )
    }

    @Test
    fun `should generate new refresh token after refresh`() = runTest {
        // Arrange: register user
        val salt = UUID.randomUUID().toString()
        val authResponse = unauthorizedClient.registerTestUser(
            email = "newrefresh-$salt@example.com",
            name = "New Refresh User",
            password = "password123"
        )

        val originalRefreshToken = authResponse.refreshToken

        // Act: refresh token
        val refreshedResponse = unauthorizedClient
            .post()
            .uri("${AUTH_URL}/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TokenToRefresh(token = originalRefreshToken))
            .exchange()
            .expectStatus().isOk
            .expectBody<AuthResponse>()
            .returnResult()
            .responseBody!!

        // Assert: new refresh token should be different
        assertTrue(originalRefreshToken != refreshedResponse.refreshToken)
    }

    @Test
    fun `should reject refresh with invalid token`() = runTest {
        val refreshRequest = TokenToRefresh(token = "invalid.token.here")

        unauthorizedClient
            .post()
            .uri("${AUTH_URL}/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(refreshRequest)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should reject refresh with empty token`() = runTest {
        val refreshRequest = TokenToRefresh(token = "")

        unauthorizedClient
            .post()
            .uri("${AUTH_URL}/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(refreshRequest)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should not allow using access token for refresh`() = runTest {
        // Arrange: register user and get tokens
        val salt = UUID.randomUUID().toString()
        val authResponse = unauthorizedClient.registerTestUser(
            email = "tokenswap-$salt@example.com",
            name = "Token Swap User",
            password = "password123"
        )

        // Act: try to use access token as refresh token
        val refreshRequest = TokenToRefresh(token = authResponse.accessToken)

        // Assert: should fail
        unauthorizedClient
            .post()
            .uri("${AUTH_URL}/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(refreshRequest)
            .exchange()
            .expectStatus().isUnauthorized
    }
}
