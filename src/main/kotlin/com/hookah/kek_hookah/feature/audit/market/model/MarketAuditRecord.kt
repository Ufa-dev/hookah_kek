package com.hookah.kek_hookah.feature.audit.market.model

import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.market.model.MarketArc
import com.hookah.kek_hookah.feature.market.model.MarketArcId
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.user.model.UserId
import java.time.OffsetDateTime
import java.util.UUID

data class MarketAuditRecord(
    val id: UUID,
    val marketArcId: MarketArcId,
    val eventType: AuditEventType,
    val brandId: BrandId,
    val flavorId: FlavorId,
    val name: String,
    val weightGrams: Int,
    val count: Int,
    val gtin: String?,
    val updatedBy: UserId,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    companion object {
        fun from(arc: MarketArc, eventType: AuditEventType) = MarketAuditRecord(
            id = UUID.randomUUID(),
            marketArcId = arc.id,
            eventType = eventType,
            brandId = arc.brandId,
            flavorId = arc.flavorId,
            name = arc.name,
            weightGrams = arc.weightGrams,
            count = arc.count,
            gtin = arc.gtin,
            updatedBy = arc.updatedBy,
            createdAt = arc.createdAt,
            updatedAt = arc.updatedAt,
        )
    }
}
