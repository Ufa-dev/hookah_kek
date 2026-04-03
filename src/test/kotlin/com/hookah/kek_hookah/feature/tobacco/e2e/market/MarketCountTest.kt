package com.hookah.kek_hookah.feature.tobacco.e2e.market

import com.hookah.kek_hookah.feature.market.model.MarketArcView
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

@IntegrationTest
class MarketCountTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should update count successfully`() = runTest {
        val client = unauthorizedClient.randomUser()
        val created = client.createMarketAndGet()

        val result = client.patchMarketCount(created.id.id, 7)
            .expectStatus().isOk
            .expectBody<MarketArcView>()
            .returnResult().responseBody!!

        assertAll(
            { assertEquals(7, result.count) },
            { assertEquals(created.id, result.id) },
            { assertTrue(result.updatedAt >= created.updatedAt) },
        )
    }

    @Test
    fun `should return 400 for negative count`() = runTest {
        val client = unauthorizedClient.randomUser()
        val created = client.createMarketAndGet()

        client.patchMarketCount(created.id.id, -1)
            .expectStatus().isBadRequest
    }

    @Test
    fun `should return 404 for non-existent market arc`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.patchMarketCount(UUID.randomUUID(), 3)
            .expectStatus().isNotFound
    }

    @Test
    fun `should return 401 when unauthenticated`() = runTest {
        unauthorizedClient.patch()
            .uri("$MARKET_URL/${UUID.randomUUID()}/count")
            .header("Content-Type", "application/json")
            .bodyValue("""{"count":1}""")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
