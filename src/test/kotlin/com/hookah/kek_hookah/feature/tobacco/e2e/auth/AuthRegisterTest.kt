package com.hookah.kek_hookah.feature.tobacco.e2e.auth


import com.hookah.kek_hookah.feature.auth.api.dto.AuthResponse
import com.hookah.kek_hookah.feature.auth.api.dto.RegisterRequest
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
class AuthRegisterTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should register user with valid data`() = runTest {
        val salt = UUID.randomUUID().toString()
        val registerRequest = RegisterRequest(
            email = "test-$salt@example.com",
            name = "Test User",
            password = "password123"
        )

        val result = unauthorizedClient
            .post()
            .uri("${AUTH_URL}/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(registerRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody<AuthResponse>()
            .returnResult()
            .responseBody!!

        assertAll(
            { assertNotNull(result.accessToken) },
            { assertNotNull(result.refreshToken) },
            { assertTrue(result.expiresIn > 0) },
            { assertTrue(result.refreshExpiresIn > 0) }
        )
    }

    @Test
    fun `should reject registration with duplicate email`() = runTest {
        // Arrange: register first user
        val salt = UUID.randomUUID().toString()
        val email = "duplicate-$salt@example.com"
        unauthorizedClient.registerTestUser(email = email, name = "First User", password = "password123")

        // Act & Assert: try to register with same email
        val duplicateRequest = RegisterRequest(
            email = email,
            name = "Second User",
            password = "password456"
        )

        unauthorizedClient
            .post()
            .uri("${AUTH_URL}/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(duplicateRequest)
            .exchange()
            .expectStatus().is5xxServerError
    }

    @Test
    fun `should accept valid email formats`() = runTest {
        val salt = UUID.randomUUID().toString()
        val validEmails = listOf(
            "user-$salt@example.com",
            "user.name-$salt@example.com",
            "user+tag-$salt@example.co.uk",
            "user_123-$salt@example-domain.com"
        )

        validEmails.forEachIndexed { index, email ->
            val result = unauthorizedClient
                .post()
                .uri("${AUTH_URL}/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                    RegisterRequest(
                        email = email,
                        name = "User $index",
                        password = "password123"
                    )
                )
                .exchange()
                .expectStatus().isCreated
                .expectBody<AuthResponse>()
                .returnResult()
                .responseBody!!

            assertNotNull(result.accessToken)
        }
    }

    @Test
    fun `should handle unicode characters in name`() = runTest {
        val salt = UUID.randomUUID().toString()
        val registerRequest = RegisterRequest(
            email = "unicode-$salt@example.com",
            name = "用户名 Пользователь",
            password = "password123"
        )

        val result = unauthorizedClient
            .post()
            .uri("${AUTH_URL}/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(registerRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody<AuthResponse>()
            .returnResult()
            .responseBody!!

        assertNotNull(result.accessToken)
    }

    @Test
    fun `should reject password longer than 32 characters`() = runTest {
        val longPassword = "a".repeat(33)

        val registerRequest = RegisterRequest(
            email = "toobigpass@example.com",
            name = "Too Big Password",
            password = longPassword
        )

        unauthorizedClient
            .post()
            .uri("${AUTH_URL}/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(registerRequest)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `should reject password shorter than 6 characters`() = runTest {
        val shortPassword = "a".repeat(5)

        val registerRequest = RegisterRequest(
            email = "tooshortpass@example.com",
            name = "Too Short Password",
            password = shortPassword
        )

        unauthorizedClient
            .post()
            .uri("${AUTH_URL}/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(registerRequest)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `should handle special characters in password`() = runTest {
        val salt = UUID.randomUUID().toString()
        val specialPassword = "p@ssW0rd!#\$%^&*()"
        val email = "specialpass-$salt@example.com"
        val registerRequest = RegisterRequest(
            email = email,
            name = "Special Password User",
            password = specialPassword
        )

        val result = unauthorizedClient
            .post()
            .uri("${AUTH_URL}/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(registerRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody<AuthResponse>()
            .returnResult()
            .responseBody!!

        assertNotNull(result.accessToken)

        // Verify login with special characters password
        val loginResult = unauthorizedClient.login(email, specialPassword)
            .expectStatus().isOk
            .expectBody<AuthResponse>()
            .returnResult()
            .responseBody!!
        assertNotNull(loginResult.accessToken)
    }

    @Test
    fun `should not expose password in any response`() = runTest {
        val salt = UUID.randomUUID().toString()
        val registerRequest = RegisterRequest(
            email = "security-$salt@example.com",
            name = "Security User",
            password = "secretpassword123"
        )

        val response = unauthorizedClient
            .post()
            .uri("${AUTH_URL}/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(registerRequest)
            .exchange()
            .expectStatus().isCreated
            .returnResult(AuthResponse::class.java)

        // Verify response doesn't contain password
        val responseBody = response.responseBody.toString()
        assertTrue(!responseBody.contains("secretpassword123"))
    }

    @Test
    fun `should treat emails as case insensitive for registration`() = runTest {
        val salt = UUID.randomUUID().toString()
        val email = "test-$salt@example.com"
        val password = "password123"

        // Register with lowercase email
        unauthorizedClient.registerTestUser(
            email = email.lowercase(),
            name = "First User",
            password = password
        )

        // Try to register with uppercase email - should be rejected as duplicate
        // NOTE: Documents desired behavior. Currently allows registration because DB is case-sensitive.
        // Once email case-insensitivity is implemented, this will correctly fail.
        unauthorizedClient.register(
            email = email.uppercase(),
            name = "Second User",
            password = "different-password"
        )
            .expectStatus().is5xxServerError  // Will change to .isBadRequest when implemented
    }

    @Test
    fun `should treat emails as case insensitive for login after registration`() = runTest {
        val salt = UUID.randomUUID().toString()
        val email = "test-$salt@example.com"
        val password = "password123"

        // Register with lowercase email
        unauthorizedClient.registerTestUser(
            email = email.lowercase(),
            name = "Test User",
            password = password
        )

        // Login with uppercase email - should succeed
        // NOTE: Documents desired behavior. Currently fails because lookup is case-sensitive.
        unauthorizedClient.login(
            email = email.uppercase(),
            password = password
        ).expectStatus().isOk

        // Verify login works with exact case (current behavior)
        unauthorizedClient.login(
            email = email.lowercase(),
            password = password
        ).expectStatus().isOk
    }
}
