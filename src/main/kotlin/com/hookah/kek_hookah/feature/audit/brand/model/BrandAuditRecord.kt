package com.hookah.kek_hookah.feature.audit.brand.model

import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import com.hookah.kek_hookah.feature.user.model.UserId
import java.time.OffsetDateTime
import java.util.UUID

data class BrandAuditRecord(
    val id: UUID,
    val brandId: BrandId,
    val eventType: AuditEventType,
    val name: String,
    val description: String?,
    val updatedBy: UserId,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    companion object {
        fun from(brand: TabacoBrand, eventType: AuditEventType) = BrandAuditRecord(
            id = UUID.randomUUID(),
            brandId = brand.id,
            eventType = eventType,
            name = brand.name,
            description = brand.description,
            updatedBy = brand.updatedBy,
            createdAt = brand.createdAt,
            updatedAt = brand.updatedAt,
        )
    }
}
