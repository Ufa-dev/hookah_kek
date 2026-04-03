package com.hookah.kek_hookah.feature.audit.e2e.flavor

import com.hookah.kek_hookah.feature.audit.flavor.model.FlavorAuditRecord
import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.AuthorizedWebTestClient
import com.hookah.kek_hookah.utils.crud.Slice
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

const val FLAVOR_AUDIT_URL = "/api/v1/audit/flavor"

fun AuthorizedWebTestClient.listFlavorAudit(
    userId: UUID? = null,
    eventType: AuditEventType? = null,
    entityId: UUID? = null,
): WebTestClient.ResponseSpec {
    val params = buildList {
        userId?.let { add("userId=$it") }
        eventType?.let { add("eventType=${it.name}") }
        entityId?.let { add("entityId=$it") }
    }.joinToString("&")
    val uri = if (params.isNotEmpty()) "$FLAVOR_AUDIT_URL?$params" else FLAVOR_AUDIT_URL
    return get().uri(uri).exchange()
}

fun AuthorizedWebTestClient.listFlavorAuditAndGet(
    userId: UUID? = null,
    eventType: AuditEventType? = null,
    entityId: UUID? = null,
): Slice<FlavorAuditRecord> =
    listFlavorAudit(userId, eventType, entityId)
        .expectStatus().isOk
        .expectBody<Slice<FlavorAuditRecord>>()
        .returnResult().responseBody!!
