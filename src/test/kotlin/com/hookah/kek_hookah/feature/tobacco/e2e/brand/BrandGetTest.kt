package com.hookah.kek_hookah.feature.tobacco.e2e.brand

import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import com.hookah.kek_hookah.utils.crud.Slice
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
class BrandGetTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should get brand by id`() = runTest {
        val client = unauthorizedClient.randomUser()
        val created = client.createBrandAndGet()

        val brand = client.getBrandById(created.id.id)
            .expectStatus().isOk
            .expectBody<TabacoBrand>()
            .returnResult().responseBody!!

        assertAll(
            { assertEquals(created.id, brand.id) },
            { assertEquals(created.name, brand.name) }
        )
    }

    @Test
    fun `should return 404 for non-existent brand id`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.getBrandById(UUID.randomUUID())
            .expectStatus().isNotFound
    }

    @Test
    fun `should get brands by name`() = runTest {
        val client = unauthorizedClient.randomUser()
        val name = "brand-${UUID.randomUUID().toString().take(8)}"
        client.createBrandAndGet(name = name)

        val brands = client.getBrandByName(name)
            .expectStatus().isOk
            .expectBody<List<TabacoBrand>>()
            .returnResult().responseBody!!

        assertTrue(brands.any { it.name == name })
    }

    @Test
    fun `should list brands`() = runTest {
        val client = unauthorizedClient.randomUser()
        val created = client.createBrandAndGet()

        val slice = client.listBrands()
            .expectStatus().isOk
            .expectBody<Slice<TabacoBrand>>()
            .returnResult().responseBody!!

        assertTrue(slice.items.any { it.id == created.id })
    }

    @Test
    fun `should return 401 when getting brand without authentication`() = runTest {
        unauthorizedClient.get()
            .uri("$BRAND_URL/id/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
