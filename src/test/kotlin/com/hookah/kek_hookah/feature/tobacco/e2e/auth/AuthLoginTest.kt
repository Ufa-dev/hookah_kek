package com.hookah.kek_hookah.feature.tobacco.e2e.auth



import com.hookah.kek_hookah.feature.auth.api.dto.AuthResponse
import com.hookah.kek_hookah.feature.auth.api.dto.CreditsToLogin
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
class AuthLoginTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should login user with correct credentials`() = runTest {
        // Arrange: register user first
        val salt = UUID.randomUUID().toString()
        val email = "login-$salt@example.com"
        val password = "password123"
        unauthorizedClient.registerTestUser(email = email, name = "Login User", password = password)

        // Act: login
        val loginRequest = CreditsToLogin(email = email, password = password)

        val result = unauthorizedClient
            .post()
            .uri("${AUTH_URL}/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginRequest)
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
    fun `should return different tokens on subsequent logins`() = runTest {
        // Arrange: register user
        val salt = UUID.randomUUID().toString()
        val email = "multilogin-$salt@example.com"
        val password = "password123"
        unauthorizedClient.registerTestUser(email = email, name = "Multi Login User", password = password)

        // Act: login twice
        val firstLogin = unauthorizedClient.login(email, password)
            .expectStatus().isOk
            .expectBody<AuthResponse>()
            .returnResult()
            .responseBody!!

        val secondLogin = unauthorizedClient.login(email, password)
            .expectStatus().isOk
            .expectBody<AuthResponse>()
            .returnResult()
            .responseBody!!

        // Assert: tokens should be different
        assertTrue(firstLogin.accessToken != secondLogin.accessToken)
        assertTrue(firstLogin.refreshToken != secondLogin.refreshToken)
    }

    @Test
    fun `should reject login with incorrect password`() = runTest {
        // Arrange: register user
        val salt = UUID.randomUUID().toString()
        val email = "wrongpass-$salt@example.com"
        unauthorizedClient.registerTestUser(email = email, name = "Wrong Pass User", password = "correctpassword")

        // Act & Assert: try to login with wrong password
        val loginRequest = CreditsToLogin(email = email, password = "wrongpassword")

        unauthorizedClient
            .post()
            .uri("${AUTH_URL}/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should reject login with non-existent email`() = runTest {
        val salt = UUID.randomUUID().toString()
        val loginRequest = CreditsToLogin(
            email = "nonexistent-$salt@example.com",
            password = "password123"
        )

        unauthorizedClient
            .post()
            .uri("${AUTH_URL}/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isUnauthorized
    }
}
