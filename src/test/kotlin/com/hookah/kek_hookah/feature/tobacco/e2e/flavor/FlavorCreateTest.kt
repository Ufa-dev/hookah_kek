package com.hookah.kek_hookah.feature.tobacco.e2e.flavor

import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.e2e.brand.createBrandAndGet
import com.hookah.kek_hookah.feature.tobacco.flavor.model.TabacoFlavor
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

@IntegrationTest
class FlavorCreateTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should create flavor successfully`() = runTest {
        val client = unauthorizedClient.randomUser()
        val brand = client.createBrandAndGet()
        val name = "flavor-${UUID.randomUUID().toString().take(8)}"

        val flavor = client.createFlavorAndGet(brandId = brand.id.id, name = name, strength = 5)

        assertAll(
            { assertNotNull(flavor.id) },
            { assertEquals(brand.id, flavor.brandId) },
            { assertEquals(name, flavor.name) },
            { assertEquals(5.toShort(), flavor.strength) },
            { assertTrue(flavor.tags.isEmpty()) },
            { assertNotNull(flavor.createdAt) },
            { assertNotNull(flavor.updatedBy) }
        )
    }

    @Test
    fun `should return 401 when creating flavor without authentication`() = runTest {
        unauthorizedClient.post()
            .uri(FLAVOR_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("brandId" to UUID.randomUUID(), "name" to "test", "strength" to 5))
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 400 when flavor name is empty`() = runTest {
        val client = unauthorizedClient.randomUser()
        val brand = client.createBrandAndGet()
        client.createFlavor(brandId = brand.id.id, name = "")
            .expectStatus().isBadRequest
    }

    @Test
    fun `should return 400 when strength is out of range`() = runTest {
        val client = unauthorizedClient.randomUser()
        val brand = client.createBrandAndGet()
        client.createFlavor(brandId = brand.id.id, strength = 11)
            .expectStatus().isBadRequest
    }
}
