package com.hookah.kek_hookah.feature.audit.pack.model

import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackId
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackTagId
import com.hookah.kek_hookah.feature.user.model.UserId
import java.time.OffsetDateTime
import java.util.UUID

data class PackAuditRecord(
    val id: UUID,
    val packId: PackId,
    val eventType: AuditEventType,
    val tagId: PackTagId,
    val name: String,
    val flavorId: FlavorId?,
    val currentWeightGrams: Int,
    val totalWeightGrams: Int,
    val updatedBy: UserId,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    companion object {
        fun from(pack: FlavorPack, eventType: AuditEventType) = PackAuditRecord(
            id = UUID.randomUUID(),
            packId = pack.id,
            eventType = eventType,
            tagId = pack.tagId,
            name = pack.name,
            flavorId = pack.flavorId,
            currentWeightGrams = pack.currentWeightGrams,
            totalWeightGrams = pack.totalWeightGrams,
            updatedBy = pack.updatedBy,
            createdAt = pack.createdAt,
            updatedAt = pack.updatedAt,
        )
    }
}
