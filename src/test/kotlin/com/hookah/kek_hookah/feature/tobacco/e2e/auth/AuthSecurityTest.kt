package com.hookah.kek_hookah.feature.tobacco.e2e.auth

import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

@IntegrationTest
class AuthSecurityTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should generate unique tokens for each user`() = runTest {
        // Arrange & Act: register two users
        val salt1 = UUID.randomUUID().toString()
        val salt2 = UUID.randomUUID().toString()

        val user1Response = unauthorizedClient.registerTestUser(
            email = "user1-$salt1@example.com",
            name = "User 1",
            password = "password123"
        )

        val user2Response = unauthorizedClient.registerTestUser(
            email = "user2-$salt2@example.com",
            name = "User 2",
            password = "password123"
        )

        // Assert: all tokens should be unique
        assertTrue(user1Response.accessToken != user2Response.accessToken)
        assertTrue(user1Response.refreshToken != user2Response.refreshToken)
    }
}
