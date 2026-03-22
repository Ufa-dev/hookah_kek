package com.hookah.kek_hookah.feature.tobacco.e2e.pack

import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.e2e.brand.createBrandAndGet
import com.hookah.kek_hookah.feature.tobacco.e2e.flavor.createFlavorAndGet
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
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
class PackGetTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should get pack by id`() = runTest {
        val client = unauthorizedClient.randomUser()
        val created = client.createPackAndGet()

        val pack = client.getPackById(created.id.id)
            .expectStatus().isOk
            .expectBody<FlavorPack>()
            .returnResult().responseBody!!

        assertAll(
            { assertEquals(created.id, pack.id) },
            { assertEquals(created.name, pack.name) }
        )
    }

    @Test
    fun `should return 404 for non-existent pack id`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.getPackById(UUID.randomUUID())
            .expectStatus().isNotFound
    }

    @Test
    fun `should list packs and include created pack`() = runTest {
        val client = unauthorizedClient.randomUser()
        val created = client.createPackAndGet()

        val slice = client.listPacks()
            .expectStatus().isOk
            .expectBody<Slice<FlavorPack>>()
            .returnResult().responseBody!!

        assertTrue(slice.items.any { it.id == created.id })
    }

    @Test
    fun `should return 401 when getting pack without authentication`() = runTest {
        unauthorizedClient.get()
            .uri("$PACK_URL/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should filter packs by name`() = runTest {
        val client = unauthorizedClient.randomUser()
        val uniqueName = "unique-${UUID.randomUUID()}"
        val created = client.createPackAndGet(name = uniqueName)

        val slice = client.listPacksFiltered(name = uniqueName)
            .expectStatus().isOk
            .expectBody<Slice<FlavorPack>>()
            .returnResult().responseBody!!

        assertTrue(slice.items.any { it.id == created.id })
    }

    @Test
    fun `should filter packs by flavorId`() = runTest {
        val client = unauthorizedClient.randomUser()
        val flavor = client.createFlavorAndGet()
        val created = client.createPackAndGet(flavorId = flavor.id.id)

        val slice = client.listPacksFiltered(flavorId = flavor.id.id)
            .expectStatus().isOk
            .expectBody<Slice<FlavorPack>>()
            .returnResult().responseBody!!

        assertTrue(slice.items.any { it.id == created.id })
    }

    @Test
    fun `should filter packs by brandId`() = runTest {
        val client = unauthorizedClient.randomUser()
        val brand = client.createBrandAndGet()
        val flavor = client.createFlavorAndGet(brandId = brand.id.id)
        val created = client.createPackAndGet(flavorId = flavor.id.id)

        val slice = client.listPacksFiltered(brandId = brand.id.id)
            .expectStatus().isOk
            .expectBody<Slice<FlavorPack>>()
            .returnResult().responseBody!!

        assertTrue(slice.items.any { it.id == created.id })
    }
}
