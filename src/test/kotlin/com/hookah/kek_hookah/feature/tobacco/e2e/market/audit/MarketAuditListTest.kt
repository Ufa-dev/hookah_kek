package com.hookah.kek_hookah.feature.tobacco.e2e.market.audit

import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.e2e.market.createMarketAndGet
import com.hookah.kek_hookah.feature.tobacco.e2e.market.deleteMarket
import com.hookah.kek_hookah.feature.tobacco.e2e.market.patchMarketCount
import com.hookah.kek_hookah.feature.tobacco.e2e.market.updateMarket
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient

@IntegrationTest
class MarketAuditListTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should record CREATED audit on market arc create`() = runTest {
        val client = unauthorizedClient.randomUser()
        val created = client.createMarketAndGet()

        val slice = client.listMarketAudit(entityId = created.id.id)
            .expectStatus().isOk
            .bodyAsMarketAuditSlice()

        assertTrue(slice.items.any { it.marketArcId == created.id && it.eventType == AuditEventType.CREATED })
    }

    @Test
    fun `should record UPDATED audit on full update`() = runTest {
        val client = unauthorizedClient.randomUser()
        val created = client.createMarketAndGet()
        client.updateMarket(
            id = created.id.id,
            brandId = created.brandId.id,
            flavorId = created.flavorId.id,
            name = "updated-name",
            count = 2,
        ).expectStatus().isOk

        val slice = client.listMarketAudit(entityId = created.id.id, eventType = AuditEventType.UPDATED)
            .expectStatus().isOk
            .bodyAsMarketAuditSlice()

        assertTrue(slice.items.any { it.marketArcId == created.id && it.eventType == AuditEventType.UPDATED })
    }

    @Test
    fun `should record UPDATED audit on count-only patch`() = runTest {
        val client = unauthorizedClient.randomUser()
        val created = client.createMarketAndGet()
        client.patchMarketCount(created.id.id, 5).expectStatus().isOk

        val slice = client.listMarketAudit(entityId = created.id.id, eventType = AuditEventType.UPDATED)
            .expectStatus().isOk
            .bodyAsMarketAuditSlice()

        assertTrue(slice.items.any { it.marketArcId == created.id })
    }

    @Test
    fun `should record DELETED audit on delete`() = runTest {
        val client = unauthorizedClient.randomUser()
        val created = client.createMarketAndGet()
        client.deleteMarket(created.id.id).expectStatus().isNoContent

        val slice = client.listMarketAudit(entityId = created.id.id, eventType = AuditEventType.DELETED)
            .expectStatus().isOk
            .bodyAsMarketAuditSlice()

        assertTrue(slice.items.any { it.marketArcId == created.id && it.eventType == AuditEventType.DELETED })
    }

    @Test
    fun `should filter by entityId — only return records for that arc`() = runTest {
        val client = unauthorizedClient.randomUser()
        val arc1 = client.createMarketAndGet()
        client.createMarketAndGet() // arc2, different entity

        val slice = client.listMarketAudit(entityId = arc1.id.id)
            .expectStatus().isOk
            .bodyAsMarketAuditSlice()

        assertTrue(slice.items.all { it.marketArcId == arc1.id })
    }

    @Test
    fun `should return 401 when unauthenticated`() = runTest {
        unauthorizedClient.get()
            .uri(MARKET_AUDIT_URL)
            .exchange()
            .expectStatus().isUnauthorized
    }
}
