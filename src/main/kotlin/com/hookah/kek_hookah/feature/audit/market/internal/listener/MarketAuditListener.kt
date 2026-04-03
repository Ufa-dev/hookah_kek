package com.hookah.kek_hookah.feature.audit.market.internal.listener

import com.hookah.kek_hookah.feature.audit.market.internal.repository.MarketAuditRepository
import com.hookah.kek_hookah.feature.audit.market.model.MarketAuditRecord
import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.market.model.MarketArcCreatedEvent
import com.hookah.kek_hookah.feature.market.model.MarketArcDeletedEvent
import com.hookah.kek_hookah.feature.market.model.MarketArcUpdatedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class MarketAuditListener(private val repository: MarketAuditRepository) {

    @EventListener
    suspend fun on(event: MarketArcCreatedEvent) {
        repository.insert(MarketAuditRecord.from(event.arc, AuditEventType.CREATED))
    }

    @EventListener
    suspend fun on(event: MarketArcUpdatedEvent) {
        repository.insert(MarketAuditRecord.from(event.before, AuditEventType.UPDATED))
    }

    @EventListener
    suspend fun on(event: MarketArcDeletedEvent) {
        repository.insert(MarketAuditRecord.from(event.arc, AuditEventType.DELETED))
    }
}
