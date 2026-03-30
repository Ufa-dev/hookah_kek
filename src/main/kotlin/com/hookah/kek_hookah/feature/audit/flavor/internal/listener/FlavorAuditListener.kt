package com.hookah.kek_hookah.feature.audit.flavor.internal.listener

import com.hookah.kek_hookah.feature.audit.flavor.internal.repository.FlavorAuditRepository
import com.hookah.kek_hookah.feature.audit.flavor.model.FlavorAuditRecord
import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorCreatedEvent
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorDeletedEvent
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorUpdatedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class FlavorAuditListener(private val repository: FlavorAuditRepository) {

    @EventListener
    suspend fun on(event: FlavorCreatedEvent) {
        repository.insert(FlavorAuditRecord.from(event.flavor, AuditEventType.CREATED))
    }

    @EventListener
    suspend fun on(event: FlavorUpdatedEvent) {
        // event.before = the OLD state before the update
        repository.insert(FlavorAuditRecord.from(event.before, AuditEventType.UPDATED))
    }

    @EventListener
    suspend fun on(event: FlavorDeletedEvent) {
        repository.insert(FlavorAuditRecord.from(event.flavor, AuditEventType.DELETED))
    }
}
