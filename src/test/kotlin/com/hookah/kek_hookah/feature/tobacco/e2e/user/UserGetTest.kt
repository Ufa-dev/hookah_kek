package com.hookah.kek_hookah.feature.tobacco.e2e.user

import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import com.hookah.kek_hookah.feature.user.model.User
import com.maverick.landcruiser.server.e2e.user.getMe

import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@IntegrationTest
class UserGetTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    // ========== Happy Path Tests ==========

    @Test
    fun `should return current user for GET me`() = runTest {
        val client = unauthorizedClient.randomUser()

        val user = client.getMe()
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        assertAll(
            { assertNotNull(user.id) },
            { assertEquals(client.userName, user.name) },
            { assertEquals(client.userEmail, user.email) },
            { assertNotNull(user.createdAt) },
            { assertNotNull(user.updatedAt) }
        )
    }
}
