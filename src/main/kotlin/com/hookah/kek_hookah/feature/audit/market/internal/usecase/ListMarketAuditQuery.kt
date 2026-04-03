package com.hookah.kek_hookah.feature.audit.market.internal.usecase

import com.hookah.kek_hookah.feature.audit.market.internal.repository.MarketAuditRepository
import com.hookah.kek_hookah.feature.audit.market.model.MarketAuditRecord
import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.utils.crud.Slice
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ListMarketAuditQuery(private val repository: MarketAuditRepository) {

    suspend fun execute(
        limit: Int,
        afterId: UUID?,
        userId: UUID?,
        eventType: AuditEventType?,
        entityId: UUID?,
    ): Slice<MarketAuditRecord> {
        val records = repository.findAll(limit, afterId, userId, eventType, entityId)
        val nextToken = if (records.size == limit) records.last().id.toString() else null
        return Slice(items = records, nextToken = nextToken)
    }
}
