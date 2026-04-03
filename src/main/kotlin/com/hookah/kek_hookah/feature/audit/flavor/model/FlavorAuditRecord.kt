package com.hookah.kek_hookah.feature.audit.flavor.model

import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.TabacoFlavor
import com.hookah.kek_hookah.feature.user.model.UserId
import java.time.OffsetDateTime
import java.util.UUID

data class FlavorAuditRecord(
    val id: UUID,
    val flavorId: FlavorId,
    val eventType: AuditEventType,
    val brandId: BrandId,
    val name: String,
    val description: String?,
    val strength: Short?,
    val updatedBy: UserId,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    companion object {
        fun from(flavor: TabacoFlavor, eventType: AuditEventType) = FlavorAuditRecord(
            id = UUID.randomUUID(),
            flavorId = flavor.id,
            eventType = eventType,
            brandId = flavor.brandId,
            name = flavor.name,
            description = flavor.description,
            strength = flavor.strength,
            updatedBy = flavor.updatedBy,
            createdAt = flavor.createdAt,
            updatedAt = flavor.updatedAt,
        )
    }
}
