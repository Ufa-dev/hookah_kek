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
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

@IntegrationTest
class MarketUpdateTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should update market arc`() = runTest {
        val client = unauthorizedClient.randomUser()
        val market = client.createMarketAndGet()
        val newName = "upd-${UUID.randomUUID().toString().take(8)}"

        val updated = client.updateMarket(
            id = market.id.id,
            brandId = market.brandId.id,
            flavorId = market.flavorId.id,
            name = newName,
            weightGrams = 500
        ).expectStatus().isOk
            .expectBody<MarketArcView>()
            .returnResult().responseBody!!

        assertAll(
            { assertEquals(market.id, updated.id) },
            { assertEquals(newName, updated.name) },
            { assertEquals(500, updated.weightGrams) },
            { assertTrue(updated.updatedAt >= market.updatedAt) }
        )
    }

    @Test
    fun `should return 401 when updating market without authentication`() = runTest {
        unauthorizedClient.put()
            .uri("$MARKET_URL/${UUID.randomUUID()}")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("brandId" to UUID.randomUUID(), "flavorId" to UUID.randomUUID(), "name" to "upd", "weightGrams" to 100))
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 400 when updating market with zero weight`() = runTest {
        val client = unauthorizedClient.randomUser()
        val market = client.createMarketAndGet()

        client.updateMarket(
            id = market.id.id,
            brandId = market.brandId.id,
            flavorId = market.flavorId.id,
            name = "valid",
            weightGrams = 0
        ).expectStatus().isBadRequest
    }
}
