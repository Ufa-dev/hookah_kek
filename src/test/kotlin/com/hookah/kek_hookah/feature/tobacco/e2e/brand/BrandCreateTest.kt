package com.hookah.kek_hookah.feature.tobacco.e2e.brand

import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
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
class BrandCreateTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should create brand successfully`() = runTest {
        val client = unauthorizedClient.randomUser()
        val name = "brand-${UUID.randomUUID().toString().take(8)}"

        val brand = client.createBrandAndGet(name = name)

        assertAll(
            { assertNotNull(brand.id) },
            { assertEquals(name, brand.name) },
            { assertNotNull(brand.createdAt) },
            { assertNotNull(brand.updatedAt) },
            { assertTrue(brand.tags.isEmpty()) }
        )
    }

    @Test
    fun `should return 401 when creating brand without authentication`() = runTest {
        unauthorizedClient.post()
            .uri(BRAND_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to "some-brand", "description" to "desc"))
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 400 when brand name is blank`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.createBrand(name = "  ")
            .expectStatus().isBadRequest
    }

    @Test
    fun `should return 400 when brand name is too short (1 char)`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.createBrand(name = "x")
            .expectStatus().isBadRequest
    }
}
