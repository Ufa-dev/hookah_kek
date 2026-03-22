package com.hookah.kek_hookah.feature.tobacco.e2e.market

import com.hookah.kek_hookah.feature.market.model.MarketArcView
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
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
class MarketGetTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should get market by id`() = runTest {
        val client = unauthorizedClient.randomUser()
        val market = client.createMarketAndGet()

        val found = client.getMarketById(market.id.id)
            .expectStatus().isOk
            .expectBody<MarketArcView>()
            .returnResult().responseBody!!

        assertAll(
            { assertEquals(market.id, found.id) },
            { assertEquals(market.name, found.name) },
            { assertEquals(market.brandName, found.brandName) },
            { assertEquals(market.flavorName, found.flavorName) }
        )
    }

    @Test
    fun `should return 404 for non-existent market id`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.getMarketById(UUID.randomUUID())
            .expectStatus().isNotFound
    }

    @Test
    fun `should list markets and include created market`() = runTest {
        val client = unauthorizedClient.randomUser()
        val market = client.createMarketAndGet()

        val markets = client.listMarkets()
            .expectStatus().isOk
            .expectBody<List<MarketArcView>>()
            .returnResult().responseBody!!

        assertTrue(markets.any { it.id == market.id })
    }

    @Test
    fun `should filter markets by brand name`() = runTest {
        val client = unauthorizedClient.randomUser()
        val market = client.createMarketAndGet()

        val markets = client.listMarkets(brandName = market.brandName)
            .expectStatus().isOk
            .expectBody<List<MarketArcView>>()
            .returnResult().responseBody!!

        assertTrue(markets.all { it.brandName == market.brandName })
    }

    @Test
    fun `should return 401 when getting market without authentication`() = runTest {
        unauthorizedClient.get()
            .uri("$MARKET_URL/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
