package com.hookah.kek_hookah.feature.audit.e2e.brand

import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.e2e.brand.createBrandAndGet
import com.hookah.kek_hookah.feature.tobacco.e2e.brand.deleteBrand
import com.hookah.kek_hookah.feature.tobacco.e2e.brand.updateBrand
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

@IntegrationTest
class BrandAuditGetTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should save CREATED audit record when brand is created`() = runTest {
        val client = unauthorizedClient.randomUser()
        val brand = client.createBrandAndGet()

        val slice = client.listBrandAuditAndGet(
            eventType = AuditEventType.CREATED,
            entityId = brand.id.id,
        )

        assertTrue(slice.items.any { it.brandId == brand.id && it.eventType == AuditEventType.CREATED })
    }

    @Test
    fun `should save UPDATED audit record with old state when brand is updated`() = runTest {
        val client = unauthorizedClient.randomUser()
        val originalName = "orig-${UUID.randomUUID().toString().take(8)}"
        val brand = client.createBrandAndGet(name = originalName)

        client.updateBrand(
            id = brand.id.id,
            name = "updated-${UUID.randomUUID().toString().take(8)}",
        ).expectStatus().isOk

        val slice = client.listBrandAuditAndGet(
            eventType = AuditEventType.UPDATED,
            entityId = brand.id.id,
        )

        // The UPDATED hist record must contain the OLD name
        assertTrue(slice.items.any { it.brandId == brand.id && it.name == originalName })
    }

    @Test
    fun `should save DELETED audit record when brand is deleted`() = runTest {
        val client = unauthorizedClient.randomUser()
        val brand = client.createBrandAndGet()

        client.deleteBrand(brand.id.id).expectStatus().isNoContent

        val slice = client.listBrandAuditAndGet(
            eventType = AuditEventType.DELETED,
            entityId = brand.id.id,
        )

        assertTrue(slice.items.any { it.brandId == brand.id && it.eventType == AuditEventType.DELETED })
    }

    @Test
    fun `should filter audit records by userId`() = runTest {
        val client = unauthorizedClient.randomUser()
        val brand = client.createBrandAndGet()

        val creatorUserId = client.listBrandAuditAndGet(entityId = brand.id.id)
            .items.first().updatedBy.id

        val sliceMatch = client.listBrandAuditAndGet(userId = creatorUserId, entityId = brand.id.id)
        val sliceNoMatch = client.listBrandAuditAndGet(userId = UUID.randomUUID(), entityId = brand.id.id)

        assertAll(
            { assertTrue(sliceMatch.items.any { it.brandId == brand.id }) },
            { assertTrue(sliceNoMatch.items.isEmpty()) },
        )
    }

    @Test
    fun `should filter audit records by eventType`() = runTest {
        val client = unauthorizedClient.randomUser()
        val brand = client.createBrandAndGet()
        client.updateBrand(
            id = brand.id.id,
            name = "upd-${UUID.randomUUID().toString().take(8)}",
        ).expectStatus().isOk

        val createdSlice = client.listBrandAuditAndGet(
            eventType = AuditEventType.CREATED,
            entityId = brand.id.id,
        )
        val updatedSlice = client.listBrandAuditAndGet(
            eventType = AuditEventType.UPDATED,
            entityId = brand.id.id,
        )

        assertAll(
            { assertTrue(createdSlice.items.isNotEmpty(), "CREATED slice must not be empty") },
            { assertTrue(createdSlice.items.all { it.eventType == AuditEventType.CREATED }) },
            { assertTrue(updatedSlice.items.isNotEmpty(), "UPDATED slice must not be empty") },
            { assertTrue(updatedSlice.items.all { it.eventType == AuditEventType.UPDATED }) },
        )
    }

    @Test
    fun `should return 401 when listing brand audit without authentication`() = runTest {
        unauthorizedClient.get()
            .uri(BRAND_AUDIT_URL)
            .exchange()
            .expectStatus().isUnauthorized
    }
}
