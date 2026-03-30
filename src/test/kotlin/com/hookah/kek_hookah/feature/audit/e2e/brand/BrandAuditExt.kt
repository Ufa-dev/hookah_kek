package com.hookah.kek_hookah.feature.audit.e2e.brand

import com.hookah.kek_hookah.feature.audit.brand.model.BrandAuditRecord
import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.AuthorizedWebTestClient
import com.hookah.kek_hookah.utils.crud.Slice
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

const val BRAND_AUDIT_URL = "/api/v1/audit/brand"

fun AuthorizedWebTestClient.listBrandAudit(
    userId: UUID? = null,
    eventType: AuditEventType? = null,
    entityId: UUID? = null,
): WebTestClient.ResponseSpec {
    val params = buildList {
        userId?.let { add("userId=$it") }
        eventType?.let { add("eventType=${it.name}") }
        entityId?.let { add("entityId=$it") }
    }.joinToString("&")
    val uri = if (params.isNotEmpty()) "$BRAND_AUDIT_URL?$params" else BRAND_AUDIT_URL
    return get().uri(uri).exchange()
}

fun AuthorizedWebTestClient.listBrandAuditAndGet(
    userId: UUID? = null,
    eventType: AuditEventType? = null,
    entityId: UUID? = null,
): Slice<BrandAuditRecord> =
    listBrandAudit(userId, eventType, entityId)
        .expectStatus().isOk
        .expectBody<Slice<BrandAuditRecord>>()
        .returnResult().responseBody!!
