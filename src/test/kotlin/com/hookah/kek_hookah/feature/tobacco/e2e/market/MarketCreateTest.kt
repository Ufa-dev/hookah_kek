package com.hookah.kek_hookah.feature.tobacco.e2e.market

import com.hookah.kek_hookah.feature.market.model.MarketArcView
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.e2e.brand.createBrandAndGet
import com.hookah.kek_hookah.feature.tobacco.e2e.flavor.createFlavorAndGet
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
class MarketCreateTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should create market arc successfully`() = runTest {
        val client = unauthorizedClient.randomUser()
        val brand = client.createBrandAndGet()
        val flavor = client.createFlavorAndGet(brandId = brand.id.id)
        val name = "market-${UUID.randomUUID().toString().take(8)}"

        val market = client.createMarket(brandId = brand.id.id, flavorId = flavor.id.id, name = name, weightGrams = 250)
            .expectStatus().isOk
            .expectBody<MarketArcView>()
            .returnResult().responseBody!!

        assertAll(
            { assertNotNull(market.id) },
            { assertEquals(brand.id, market.brandId) },
            { assertEquals(brand.name, market.brandName) },
            { assertEquals(flavor.id, market.flavorId) },
            { assertEquals(flavor.name, market.flavorName) },
            { assertEquals(name, market.name) },
            { assertEquals(250, market.weightGrams) },
            { assertNotNull(market.createdAt) },
            { assertNotNull(market.updatedBy) }
        )
    }

    @Test
    fun `should return 401 when creating market without authentication`() = runTest {
        unauthorizedClient.post()
            .uri(MARKET_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("brandId" to UUID.randomUUID(), "flavorId" to UUID.randomUUID(), "name" to "m", "weightGrams" to 100))
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 400 when market name is blank`() = runTest {
        val client = unauthorizedClient.randomUser()
        val brand = client.createBrandAndGet()
        val flavor = client.createFlavorAndGet(brandId = brand.id.id)

        client.createMarket(brandId = brand.id.id, flavorId = flavor.id.id, name = "  ")
            .expectStatus().isBadRequest
    }

    @Test
    fun `should return 400 when weightGrams is zero`() = runTest {
        val client = unauthorizedClient.randomUser()
        val brand = client.createBrandAndGet()
        val flavor = client.createFlavorAndGet(brandId = brand.id.id)

        client.createMarket(brandId = brand.id.id, flavorId = flavor.id.id, weightGrams = 0)
            .expectStatus().isBadRequest
    }
}
