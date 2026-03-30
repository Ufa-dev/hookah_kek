package com.hookah.kek_hookah.feature.audit.pack.internal.listener

import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.audit.pack.internal.repository.PackAuditRepository
import com.hookah.kek_hookah.feature.audit.pack.model.PackAuditRecord
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackCreatedEvent
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackDeletedEvent
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackUpdatedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class PackAuditListener(private val repository: PackAuditRepository) {

    @EventListener
    suspend fun on(event: PackCreatedEvent) =
        repository.insert(PackAuditRecord.from(event.pack, AuditEventType.CREATED))

    @EventListener
    suspend fun on(event: PackUpdatedEvent) =
        repository.insert(PackAuditRecord.from(event.before, AuditEventType.UPDATED))

    @EventListener
    suspend fun on(event: PackDeletedEvent) =
        repository.insert(PackAuditRecord.from(event.pack, AuditEventType.DELETED))
}
