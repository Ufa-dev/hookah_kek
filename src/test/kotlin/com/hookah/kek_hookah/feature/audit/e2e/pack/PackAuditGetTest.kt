package com.hookah.kek_hookah.feature.audit.e2e.pack

import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.e2e.pack.createPackAndGet
import com.hookah.kek_hookah.feature.tobacco.e2e.pack.deletePack
import com.hookah.kek_hookah.feature.tobacco.e2e.pack.updatePack
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID
import kotlin.test.assertTrue

@IntegrationTest
class PackAuditGetTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should save CREATED audit record when pack is created`() = runTest {
        val client = unauthorizedClient.randomUser()
        val pack = client.createPackAndGet()
        val slice = client.listPackAuditAndGet(eventType = AuditEventType.CREATED, entityId = pack.id.id)
        assertTrue(slice.items.any { it.packId == pack.id && it.eventType == AuditEventType.CREATED })
    }

    @Test
    fun `should save UPDATED audit record with old state when pack is updated`() = runTest {
        val client = unauthorizedClient.randomUser()
        val originalName = "orig-${UUID.randomUUID().toString().take(8)}"
        val pack = client.createPackAndGet(name = originalName)
        client.updatePack(id = pack.id.id, name = "updated-${UUID.randomUUID().toString().take(8)}").expectStatus().isOk
        val slice = client.listPackAuditAndGet(eventType = AuditEventType.UPDATED, entityId = pack.id.id)
        assertTrue(slice.items.any { it.packId == pack.id && it.name == originalName })
    }

    @Test
    fun `should save DELETED audit record when pack is deleted`() = runTest {
        val client = unauthorizedClient.randomUser()
        val pack = client.createPackAndGet()
        client.deletePack(pack.id.id).expectStatus().isNoContent
        val slice = client.listPackAuditAndGet(eventType = AuditEventType.DELETED, entityId = pack.id.id)
        assertTrue(slice.items.any { it.packId == pack.id && it.eventType == AuditEventType.DELETED })
    }

    @Test
    fun `should filter audit records by eventType`() = runTest {
        val client = unauthorizedClient.randomUser()
        val pack = client.createPackAndGet()
        client.updatePack(id = pack.id.id, name = "upd-${UUID.randomUUID().toString().take(8)}").expectStatus().isOk
        val createdSlice = client.listPackAuditAndGet(eventType = AuditEventType.CREATED, entityId = pack.id.id)
        val updatedSlice = client.listPackAuditAndGet(eventType = AuditEventType.UPDATED, entityId = pack.id.id)
        assertAll(
            { assertTrue(createdSlice.items.isNotEmpty(), "CREATED slice must not be empty") },
            { assertTrue(createdSlice.items.all { it.eventType == AuditEventType.CREATED }) },
            { assertTrue(updatedSlice.items.isNotEmpty(), "UPDATED slice must not be empty") },
            { assertTrue(updatedSlice.items.all { it.eventType == AuditEventType.UPDATED }) },
        )
    }

    @Test
    fun `should filter audit records by userId`() = runTest {
        val client = unauthorizedClient.randomUser()
        val pack = client.createPackAndGet()

        val creatorUserId = client.listPackAuditAndGet(entityId = pack.id.id)
            .items.first().updatedBy.id

        val sliceMatch = client.listPackAuditAndGet(userId = creatorUserId, entityId = pack.id.id)
        val sliceNoMatch = client.listPackAuditAndGet(userId = UUID.randomUUID(), entityId = pack.id.id)

        assertAll(
            { assertTrue(sliceMatch.items.any { it.packId == pack.id }) },
            { assertTrue(sliceNoMatch.items.isEmpty()) },
        )
    }

    @Test
    fun `should return 401 when listing pack audit without authentication`() = runTest {
        unauthorizedClient.get().uri(PACK_AUDIT_URL).exchange().expectStatus().isUnauthorized
    }
}
