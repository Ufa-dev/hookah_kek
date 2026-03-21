package com.hookah.kek_hookah.feature.tobacco.e2e.tags

import com.hookah.kek_hookah.feature.tags.model.Tag
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import com.hookah.kek_hookah.utils.crud.Slice
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

@IntegrationTest
class TagGetTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should get tag by id`() = runTest {
        val client = unauthorizedClient.randomUser()
        val name = "tst-${UUID.randomUUID().toString().take(8)}"
        val created = client.createTagAndGet(name)

        val tag = client.getTagById(created.id.id)
            .expectStatus().isOk
            .expectBody<Tag>()
            .returnResult().responseBody!!

        assertAll(
            { assertEquals(created.id, tag.id) },
            { assertEquals(name, tag.name) }
        )
    }

    @Test
    fun `should return 404 for non-existent tag id`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.getTagById(UUID.randomUUID())
            .expectStatus().isNotFound
    }

    @Test
    fun `should get tag by name`() = runTest {
        val client = unauthorizedClient.randomUser()
        val name = "tst-${UUID.randomUUID().toString().take(8)}"
        client.createTagAndGet(name)

        val tag = client.getTagByName(name)
            .expectStatus().isOk
            .expectBody<Tag>()
            .returnResult().responseBody!!

        assertEquals(name, tag.name)
    }

    @Test
    fun `should return 404 for non-existent tag name`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.getTagByName("nonexistent-xyz-${UUID.randomUUID()}")
            .expectStatus().isNotFound
    }

    @Test
    fun `should list tags and include created tag`() = runTest {
        val client = unauthorizedClient.randomUser()
        val name = "tst-${UUID.randomUUID().toString().take(8)}"
        client.createTagAndGet(name)

        val slice = client.listTags()
            .expectStatus().isOk
            .expectBody<Slice<Tag>>()
            .returnResult().responseBody!!

        assertNotNull(slice)
        assertTrue(slice.items.isNotEmpty())
    }

    @Test
    fun `should return 401 when getting tag without authentication`() = runTest {
        unauthorizedClient.get()
            .uri("$TAG_URL/id/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
