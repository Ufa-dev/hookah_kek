package com.hookah.kek_hookah.feature.tobacco.e2e.flavor

import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.e2e.tags.createTagAndGet
import com.hookah.kek_hookah.feature.tobacco.flavor.model.TabacoFlavor
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
class FlavorUpdateTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should update flavor`() = runTest {
        val client = unauthorizedClient.randomUser()
        val flavor = client.createFlavorAndGet()
        val newName = "upd-${UUID.randomUUID().toString().take(8)}"

        val updated = client.updateFlavor(
            id = flavor.id.id,
            brandId = flavor.brandId.id,
            name = newName,
            strength = 3
        ).expectStatus().isOk
            .expectBody<TabacoFlavor>()
            .returnResult().responseBody!!

        assertAll(
            { assertEquals(flavor.id, updated.id) },
            { assertEquals(newName, updated.name) },
            { assertEquals(3.toShort(), updated.strength) },
            { assertTrue(updated.updatedAt >= flavor.updatedAt) }
        )
    }

    @Test
    fun `should return 401 when updating flavor without authentication`() = runTest {
        unauthorizedClient.put()
            .uri("$FLAVOR_URL/${UUID.randomUUID()}")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("brandId" to UUID.randomUUID(), "name" to "new", "strength" to 5))
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 400 when updating with invalid strength`() = runTest {
        val client = unauthorizedClient.randomUser()
        val flavor = client.createFlavorAndGet()

        client.updateFlavor(
            id = flavor.id.id,
            brandId = flavor.brandId.id,
            name = "valid-name",
            strength = -1
        ).expectStatus().isBadRequest
    }

    @Test
    fun `should add tag to flavor`() = runTest {
        val client = unauthorizedClient.randomUser()
        val flavor = client.createFlavorAndGet()
        val tag = client.createTagAndGet("tst-${UUID.randomUUID().toString().take(8)}")

        val updated = client.addTagToFlavor(flavorId = flavor.id, tagId = tag.id)
            .expectStatus().isOk
            .expectBody<TabacoFlavor>()
            .returnResult().responseBody!!

        assertTrue(updated.tags.any { it.id == tag.id })
    }

    @Test
    fun `should remove tag from flavor`() = runTest {
        val client = unauthorizedClient.randomUser()
        val flavor = client.createFlavorAndGet()
        val tag = client.createTagAndGet("tst-${UUID.randomUUID().toString().take(8)}")

        client.addTagToFlavor(flavorId = flavor.id, tagId = tag.id).expectStatus().isOk

        val updated = client.removeTagFromFlavor(flavorId = flavor.id, tagId = tag.id)
            .expectStatus().isOk
            .expectBody<TabacoFlavor>()
            .returnResult().responseBody!!

        assertTrue(updated.tags.none { it.id == tag.id })
    }
}
