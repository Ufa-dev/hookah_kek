package com.hookah.kek_hookah.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class BaseApiIT {

    companion object {
        @Container
        @ServiceConnection
        @JvmField
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("hookah_test")
            .withUsername("test")
            .withPassword("test")
    }

    @LocalServerPort
    private var port: Int = 0

    val client: WebTestClient by lazy {
        WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port")
            .build()
    }

    // ─── Auth helpers ─────────────────────────────────────────────────────────

    /**
     * Registers a fresh user with a random email and returns a valid access token.
     * Each call creates a unique user to avoid conflicts between tests.
     */
    fun freshToken(): String {
        val uid = UUID.randomUUID().toString().take(8)
        val email = "test-$uid@example.com"
        val password = "Pass123!"

        client.post().uri("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("email" to email, "name" to "Test $uid", "password" to password))
            .exchange()
            .expectStatus().isOk

        val body = client.post().uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("email" to email, "password" to password))
            .exchange()
            .expectStatus().isOk
            .expectBody(Map::class.java)
            .returnResult().responseBody!!

        return body["accessToken"] as String
    }

    /** Returns a WebTestClient pre-configured with a Bearer token. */
    fun authedClient(token: String): WebTestClient =
        client.mutate()
            .defaultHeader("Authorization", "Bearer $token")
            .build()
}
