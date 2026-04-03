package com.hookah.kek_hookah.feature.audit.flavor.internal.usecase

import com.hookah.kek_hookah.feature.audit.flavor.internal.repository.FlavorAuditRepository
import com.hookah.kek_hookah.feature.audit.flavor.model.FlavorAuditRecord
import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.utils.crud.Slice
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ListFlavorAuditQuery(private val repository: FlavorAuditRepository) {

    suspend fun execute(
        limit: Int,
        afterId: UUID?,
        userId: UUID?,
        eventType: AuditEventType?,
        entityId: UUID?,
    ): Slice<FlavorAuditRecord> {
        val records = repository.findAll(limit, afterId, userId, eventType, entityId)
        val nextToken = if (records.size == limit) records.last().id.toString() else null
        return Slice(items = records, nextToken = nextToken)
    }
}
