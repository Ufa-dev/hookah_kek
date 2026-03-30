package com.hookah.kek_hookah.feature.audit.e2e.flavor

import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.e2e.flavor.createFlavorAndGet
import com.hookah.kek_hookah.feature.tobacco.e2e.flavor.deleteFlavor
import com.hookah.kek_hookah.feature.tobacco.e2e.flavor.updateFlavor
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

@IntegrationTest
class FlavorAuditGetTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should save CREATED audit record when flavor is created`() = runTest {
        val client = unauthorizedClient.randomUser()
        val flavor = client.createFlavorAndGet()

        val slice = client.listFlavorAuditAndGet(
            eventType = AuditEventType.CREATED,
            entityId = flavor.id.id,
        )

        assertTrue(slice.items.any { it.flavorId == flavor.id && it.eventType == AuditEventType.CREATED })
    }

    @Test
    fun `should save UPDATED audit record with old state when flavor is updated`() = runTest {
        val client = unauthorizedClient.randomUser()
        val originalName = "flv-orig-${UUID.randomUUID().toString().take(8)}"
        val flavor = client.createFlavorAndGet(name = originalName)

        client.updateFlavor(
            id = flavor.id.id,
            brandId = flavor.brandId.id,
            name = "flv-upd-${UUID.randomUUID().toString().take(8)}",
        ).expectStatus().isOk

        val slice = client.listFlavorAuditAndGet(
            eventType = AuditEventType.UPDATED,
            entityId = flavor.id.id,
        )

        // The UPDATED hist record must contain the OLD name
        assertTrue(slice.items.any { it.flavorId == flavor.id && it.name == originalName })
    }

    @Test
    fun `should save DELETED audit record when flavor is deleted`() = runTest {
        val client = unauthorizedClient.randomUser()
        val flavor = client.createFlavorAndGet()

        client.deleteFlavor(flavor.id.id).expectStatus().isNoContent

        val slice = client.listFlavorAuditAndGet(
            eventType = AuditEventType.DELETED,
            entityId = flavor.id.id,
        )

        assertTrue(slice.items.any { it.flavorId == flavor.id && it.eventType == AuditEventType.DELETED })
    }

    @Test
    fun `should filter flavor audit by eventType`() = runTest {
        val client = unauthorizedClient.randomUser()
        val flavor = client.createFlavorAndGet()
        client.updateFlavor(
            id = flavor.id.id,
            brandId = flavor.brandId.id,
            name = "flv-upd2-${UUID.randomUUID().toString().take(8)}",
        ).expectStatus().isOk

        val createdSlice = client.listFlavorAuditAndGet(
            eventType = AuditEventType.CREATED,
            entityId = flavor.id.id,
        )
        val updatedSlice = client.listFlavorAuditAndGet(
            eventType = AuditEventType.UPDATED,
            entityId = flavor.id.id,
        )

        assertAll(
            { assertTrue(createdSlice.items.isNotEmpty(), "CREATED slice must not be empty") },
            { assertTrue(createdSlice.items.all { it.eventType == AuditEventType.CREATED }) },
            { assertTrue(updatedSlice.items.isNotEmpty(), "UPDATED slice must not be empty") },
            { assertTrue(updatedSlice.items.all { it.eventType == AuditEventType.UPDATED }) },
        )
    }

    @Test
    fun `should return 401 when listing flavor audit without authentication`() = runTest {
        unauthorizedClient.get()
            .uri(FLAVOR_AUDIT_URL)
            .exchange()
            .expectStatus().isUnauthorized
    }
}
