package com.hookah.kek_hookah.feature.tobacco.e2e.tags

import com.hookah.kek_hookah.feature.tags.model.Tag
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

@IntegrationTest
class TagUpdateTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should update tag name`() = runTest {
        val client = unauthorizedClient.randomUser()
        val original = client.createTagAndGet("tst-${UUID.randomUUID().toString().take(8)}")
        val newName = "upd-${UUID.randomUUID().toString().take(8)}"

        val updated = client.updateTagName(original.id.id, newName)
            .expectStatus().isOk
            .expectBody<Tag>()
            .returnResult().responseBody!!

        assertAll(
            { assertEquals(original.id, updated.id) },
            { assertEquals(newName, updated.name) },
            { assertTrue(updated.updatedAt >= original.updatedAt) }
        )
    }

    @Test
    fun `should return 401 when updating tag without authentication`() = runTest {
        val client = unauthorizedClient.randomUser()
        val created = client.createTagAndGet("tst-${UUID.randomUUID().toString().take(8)}")

        unauthorizedClient.patch()
            .uri("$TAG_URL/${created.id.id}/name")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to "new-name"))
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 400 when updating with blank name`() = runTest {
        val client = unauthorizedClient.randomUser()
        val created = client.createTagAndGet("tst-${UUID.randomUUID().toString().take(8)}")

        client.updateTagName(created.id.id, "  ")
            .expectStatus().isBadRequest
    }
}
