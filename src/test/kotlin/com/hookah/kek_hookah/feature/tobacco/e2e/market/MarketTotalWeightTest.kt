package com.hookah.kek_hookah.feature.tobacco.e2e.market

import com.hookah.kek_hookah.feature.market.model.MarketTotalWeightView
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.e2e.brand.createBrandAndGet
import com.hookah.kek_hookah.feature.tobacco.e2e.flavor.createFlavorAndGet
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

@IntegrationTest
class MarketTotalWeightTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should return total weight for flavor`() = runTest {
        val client = unauthorizedClient.randomUser()
        val brand = client.createBrandAndGet()
        val flavor = client.createFlavorAndGet(brandId = brand.id.id)

        // arc1: 100g * 3 = 300; arc2: 50g * 2 = 100 → total = 400
        client.createMarket(brandId = brand.id.id, flavorId = flavor.id.id, weightGrams = 100, count = 3)
            .expectStatus().isOk
        client.createMarket(brandId = brand.id.id, flavorId = flavor.id.id, weightGrams = 50, count = 2)
            .expectStatus().isOk

        val result = client.getMarketTotalWeight(flavor.id.id)
            .expectStatus().isOk
            .expectBody<MarketTotalWeightView>()
            .returnResult().responseBody!!

        assertEquals(400L, result.totalWeightGrams)
    }

    @Test
    fun `should return 0 for flavor with no market arcs`() = runTest {
        val client = unauthorizedClient.randomUser()

        val result = client.getMarketTotalWeight(UUID.randomUUID())
            .expectStatus().isOk
            .expectBody<MarketTotalWeightView>()
            .returnResult().responseBody!!

        assertEquals(0L, result.totalWeightGrams)
    }

    @Test
    fun `should return 401 when unauthenticated`() = runTest {
        unauthorizedClient.get()
            .uri("$MARKET_URL/total-weight/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
