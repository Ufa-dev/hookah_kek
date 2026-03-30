package com.hookah.kek_hookah.feature.audit.brand.internal.usecase

import com.hookah.kek_hookah.feature.audit.brand.internal.repository.BrandAuditRepository
import com.hookah.kek_hookah.feature.audit.brand.model.BrandAuditRecord
import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.utils.crud.Slice
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ListBrandAuditQuery(private val repository: BrandAuditRepository) {

    suspend fun execute(
        limit: Int,
        afterId: UUID?,
        userId: UUID?,
        eventType: AuditEventType?,
        entityId: UUID?,
    ): Slice<BrandAuditRecord> {
        val records = repository.findAll(limit, afterId, userId, eventType, entityId)
        val nextToken = if (records.size == limit) records.last().id.toString() else null
        return Slice(items = records, nextToken = nextToken)
    }
}
