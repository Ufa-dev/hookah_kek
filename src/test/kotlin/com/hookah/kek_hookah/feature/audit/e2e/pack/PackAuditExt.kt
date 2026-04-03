package com.hookah.kek_hookah.feature.audit.e2e.pack

import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.audit.pack.model.PackAuditRecord
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.AuthorizedWebTestClient
import com.hookah.kek_hookah.utils.crud.Slice
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

const val PACK_AUDIT_URL = "/api/v1/audit/pack"

fun AuthorizedWebTestClient.listPackAudit(
    userId: UUID? = null,
    eventType: AuditEventType? = null,
    entityId: UUID? = null,
): WebTestClient.ResponseSpec {
    val params = buildList {
        userId?.let { add("userId=$it") }
        eventType?.let { add("eventType=${it.name}") }
        entityId?.let { add("entityId=$it") }
    }.joinToString("&")
    val uri = if (params.isNotEmpty()) "$PACK_AUDIT_URL?$params" else PACK_AUDIT_URL
    return get().uri(uri).exchange()
}

fun AuthorizedWebTestClient.listPackAuditAndGet(
    userId: UUID? = null,
    eventType: AuditEventType? = null,
    entityId: UUID? = null,
): Slice<PackAuditRecord> =
    listPackAudit(userId, eventType, entityId)
        .expectStatus().isOk
        .expectBody<Slice<PackAuditRecord>>()
        .returnResult().responseBody!!
