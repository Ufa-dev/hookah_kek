package com.hookah.kek_hookah.feature.tobacco.e2e.brand

import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.e2e.tags.createTagAndGet
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
class BrandUpdateTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should update brand name and description`() = runTest {
        val client = unauthorizedClient.randomUser()
        val brand = client.createBrandAndGet()
        val newName = "updated-${UUID.randomUUID().toString().take(8)}"

        val updated = client.updateBrand(id = brand.id.id, name = newName, description = "Updated desc")
            .expectStatus().isOk
            .expectBody<TabacoBrand>()
            .returnResult().responseBody!!

        assertAll(
            { assertEquals(brand.id, updated.id) },
            { assertEquals(newName, updated.name) },
            { assertEquals("Updated desc", updated.description) },
            { assertTrue(updated.updatedAt >= brand.updatedAt) }
        )
    }

    @Test
    fun `should return 401 when updating brand without authentication`() = runTest {
        unauthorizedClient.put()
            .uri("$BRAND_URL/${UUID.randomUUID()}")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to "new-name"))
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 400 when updating with blank name`() = runTest {
        val client = unauthorizedClient.randomUser()
        val brand = client.createBrandAndGet()

        client.updateBrand(id = brand.id.id, name = "  ")
            .expectStatus().isBadRequest
    }

    @Test
    fun `should add tag to brand`() = runTest {
        val client = unauthorizedClient.randomUser()
        val brand = client.createBrandAndGet()
        val tag = client.createTagAndGet("tst-${UUID.randomUUID().toString().take(8)}")

        val updated = client.addTagToBrand(brandId = brand.id, tagId = tag.id)
            .expectStatus().isOk
            .expectBody<TabacoBrand>()
            .returnResult().responseBody!!

        assertTrue(updated.tags.any { it.id == tag.id })
    }

    @Test
    fun `should remove tag from brand`() = runTest {
        val client = unauthorizedClient.randomUser()
        val brand = client.createBrandAndGet()
        val tag = client.createTagAndGet("tst-${UUID.randomUUID().toString().take(8)}")

        client.addTagToBrand(brandId = brand.id, tagId = tag.id).expectStatus().isOk

        val updated = client.removeTagFromBrand(brandId = brand.id, tagId = tag.id)
            .expectStatus().isOk
            .expectBody<TabacoBrand>()
            .returnResult().responseBody!!

        assertTrue(updated.tags.none { it.id == tag.id })
    }
}
