package com.hookah.kek_hookah.feature.tobacco.e2e.tags

import com.hookah.kek_hookah.feature.tags.model.Tag
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

@IntegrationTest
class TagCreateTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should create tag successfully`() = runTest {
        val client = unauthorizedClient.randomUser()
        val name = "tst-${UUID.randomUUID().toString().take(8)}"

        val tag = client.createTag(name)
            .expectStatus().is2xxSuccessful
            .expectBody<Tag>()
            .returnResult().responseBody!!

        assertAll(
            { assertNotNull(tag.id) },
            { assertEquals(name, tag.name) },
            { assertNotNull(tag.createdAt) },
            { assertNotNull(tag.updatedAt) }
        )
    }

    @Test
    fun `should return 401 when creating tag without authentication`() = runTest {
        unauthorizedClient.post()
            .uri(TAG_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to "some-tag"))
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 400 when tag name is blank`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.createTag("  ")
            .expectStatus().isBadRequest
    }

    @Test
    fun `should return 400 when tag name is too short (less than 3 chars)`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.createTag("ab")
            .expectStatus().isBadRequest
    }
}
