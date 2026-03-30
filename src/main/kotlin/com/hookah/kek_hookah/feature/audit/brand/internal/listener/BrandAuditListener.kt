package com.hookah.kek_hookah.feature.audit.brand.internal.listener

import com.hookah.kek_hookah.feature.audit.brand.internal.repository.BrandAuditRepository
import com.hookah.kek_hookah.feature.audit.brand.model.BrandAuditRecord
import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandCreatedEvent
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandDeletedEvent
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandUpdatedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BrandAuditListener(private val repository: BrandAuditRepository) {

    @EventListener
    suspend fun on(event: BrandCreatedEvent) {
        repository.insert(BrandAuditRecord.from(event.brand, AuditEventType.CREATED))
    }

    @EventListener
    suspend fun on(event: BrandUpdatedEvent) {
        // event.before = the OLD state before the update — that is what goes to history
        repository.insert(BrandAuditRecord.from(event.before, AuditEventType.UPDATED))
    }

    @EventListener
    suspend fun on(event: BrandDeletedEvent) {
        repository.insert(BrandAuditRecord.from(event.brand, AuditEventType.DELETED))
    }
}
