package com.hookah.kek_hookah.feature.tobacco.e2e.market

import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

@IntegrationTest
class MarketDeleteTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should delete market arc successfully`() = runTest {
        val client = unauthorizedClient.randomUser()
        val market = client.createMarketAndGet()

        client.deleteMarket(market.id.id)
            .expectStatus().isNoContent

        client.getMarketById(market.id.id)
            .expectStatus().isNotFound
    }

    @Test
    fun `should return 401 when deleting market without authentication`() = runTest {
        unauthorizedClient.delete()
            .uri("$MARKET_URL/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
