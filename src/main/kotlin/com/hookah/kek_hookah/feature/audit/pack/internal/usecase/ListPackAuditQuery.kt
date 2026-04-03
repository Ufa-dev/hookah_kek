package com.hookah.kek_hookah.feature.audit.pack.internal.usecase

import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.audit.pack.internal.repository.PackAuditRepository
import com.hookah.kek_hookah.feature.audit.pack.model.PackAuditRecord
import com.hookah.kek_hookah.utils.crud.Slice
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ListPackAuditQuery(private val repository: PackAuditRepository) {

    suspend fun execute(
        limit: Int,
        afterId: UUID?,
        userId: UUID?,
        eventType: AuditEventType?,
        entityId: UUID?,
    ): Slice<PackAuditRecord> {
        val records = repository.findAll(limit, afterId, userId, eventType, entityId)
        val nextToken = if (records.size == limit) records.last().id.toString() else null
        return Slice(items = records, nextToken = nextToken)
    }
}
