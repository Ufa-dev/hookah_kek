package com.hookah.kek_hookah.feature.tobacco.e2e.market.audit

import com.hookah.kek_hookah.feature.audit.market.model.MarketAuditRecord
import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.AuthorizedWebTestClient
import com.hookah.kek_hookah.utils.crud.Slice
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

const val MARKET_AUDIT_URL = "/api/v1/audit/market"

fun AuthorizedWebTestClient.listMarketAudit(
    entityId: UUID? = null,
    eventType: AuditEventType? = null,
    limit: Int = 20,
): WebTestClient.ResponseSpec {
    val params = buildList {
        add("limit=$limit")
        if (entityId != null)   add("entityId=$entityId")
        if (eventType != null)  add("eventType=${eventType.name}")
    }.joinToString("&")
    return get().uri("$MARKET_AUDIT_URL?$params").exchange()
}

fun WebTestClient.ResponseSpec.bodyAsMarketAuditSlice(): Slice<MarketAuditRecord> =
    expectBody(object : ParameterizedTypeReference<Slice<MarketAuditRecord>>() {})
        .returnResult().responseBody!!
