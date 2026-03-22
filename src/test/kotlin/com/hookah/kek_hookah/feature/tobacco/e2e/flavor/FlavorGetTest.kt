package com.hookah.kek_hookah.feature.tobacco.e2e.flavor

import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.e2e.brand.createBrandAndGet
import com.hookah.kek_hookah.feature.tobacco.flavor.model.TabacoFlavor
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

@IntegrationTest
class FlavorGetTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should get flavor by id`() = runTest {
        val client = unauthorizedClient.randomUser()
        val flavor = client.createFlavorAndGet()

        val found = client.getFlavorById(flavor.id.id)
            .expectStatus().isOk
            .expectBody<TabacoFlavor>()
            .returnResult().responseBody!!

        assertAll(
            { assertEquals(flavor.id, found.id) },
            { assertEquals(flavor.name, found.name) }
        )
    }

    @Test
    fun `should return 404 for non-existent flavor id`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.getFlavorById(UUID.randomUUID())
            .expectStatus().isNotFound
    }

    @Test
    fun `should get flavors by name`() = runTest {
        val client = unauthorizedClient.randomUser()
        val name = "flavor-${UUID.randomUUID().toString().take(8)}"
        client.createFlavorAndGet(name = name)

        val flavors = client.getFlavorsByName(name)
            .expectStatus().isOk
            .expectBody<List<TabacoFlavor>>()
            .returnResult().responseBody!!

        assertTrue(flavors.any { it.name == name })
    }

    @Test
    fun `should get flavors by brand id`() = runTest {
        val client = unauthorizedClient.randomUser()
        val brand = client.createBrandAndGet()
        client.createFlavorAndGet(brandId = brand.id.id)

        val flavors = client.getFlavorsByBrand(brand.id.id)
            .expectStatus().isOk
            .expectBody<List<TabacoFlavor>>()
            .returnResult().responseBody!!

        assertTrue(flavors.all { it.brandId == brand.id })
    }

    @Test
    fun `should list all flavors`() = runTest {
        val client = unauthorizedClient.randomUser()
        val flavor = client.createFlavorAndGet()

        val flavors = client.listFlavors()
            .expectStatus().isOk
            .expectBody<List<TabacoFlavor>>()
            .returnResult().responseBody!!

        assertTrue(flavors.any { it.id == flavor.id })
    }

    @Test
    fun `should search flavors by brand and name`() = runTest {
        val client = unauthorizedClient.randomUser()
        val brand = client.createBrandAndGet()
        val name = "flavor-${UUID.randomUUID().toString().take(8)}"
        client.createFlavorAndGet(brandId = brand.id.id, name = name)

        val flavors = client.searchFlavors(brandId = brand.id.id, name = name)
            .expectStatus().isOk
            .expectBody<List<TabacoFlavor>>()
            .returnResult().responseBody!!

        assertTrue(flavors.any { it.name == name })
    }

    @Test
    fun `should return 401 when getting flavor without authentication`() = runTest {
        unauthorizedClient.get()
            .uri("$FLAVOR_URL/id/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
