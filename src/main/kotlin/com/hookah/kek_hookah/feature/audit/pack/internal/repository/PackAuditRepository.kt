package com.hookah.kek_hookah.feature.audit.pack.internal.repository

import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.audit.pack.model.PackAuditRecord
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackId
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackTagId
import com.hookah.kek_hookah.feature.user.model.UserId
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class PackAuditRepository(private val template: R2dbcEntityTemplate) {

    suspend fun insert(record: PackAuditRecord): PackAuditRecord =
        template.insert(record.toEntity()).awaitSingle().toRecord()

    suspend fun findAll(
        limit: Int,
        afterId: UUID?,
        userId: UUID?,
        eventType: AuditEventType?,
        entityId: UUID?,
    ): List<PackAuditRecord> {
        var criteria = if (afterId != null) where("id").greaterThan(afterId) else null
        if (userId != null) {
            val f = where("updated_by").`is`(userId)
            criteria = criteria?.and(f) ?: f
        }
        if (eventType != null) {
            val f = where("event_type").`is`(eventType.name)
            criteria = criteria?.and(f) ?: f
        }
        if (entityId != null) {
            val f = where("flavor_pack_id").`is`(entityId)
            criteria = criteria?.and(f) ?: f
        }
        val query = if (criteria != null) Query.query(criteria) else Query.empty()
        return template.select(PackAuditEntity::class.java)
            .matching(query.sort(Sort.by(Sort.Direction.ASC, "id")).limit(limit))
            .all().collectList().awaitSingle().map { it.toRecord() }
    }

    private fun PackAuditRecord.toEntity() = PackAuditEntity(
        id = id,
        flavor_pack_id = packId.id,
        event_type = eventType.name,
        tag_id = tagId.id,
        name = name,
        flavor_id = flavorId?.id,
        current_weight_grams = currentWeightGrams,
        total_weight_grams = totalWeightGrams,
        created_at = createdAt,
        updated_at = updatedAt,
        updated_by = updatedBy.id,
    )

    private fun PackAuditEntity.toRecord() = PackAuditRecord(
        id = id,
        packId = PackId(flavor_pack_id),
        eventType = AuditEventType.valueOf(event_type),
        tagId = PackTagId(tag_id),
        name = name,
        flavorId = flavor_id?.let { FlavorId(it) },
        currentWeightGrams = current_weight_grams,
        totalWeightGrams = total_weight_grams,
        updatedBy = UserId(updated_by),
        createdAt = created_at,
        updatedAt = updated_at,
    )

    @Table("flavor_pack_hist")
    data class PackAuditEntity(
        @Id val id: UUID,
        @Column("flavor_pack_id") val flavor_pack_id: UUID,
        @Column("event_type") val event_type: String,
        @Column("tag_id") val tag_id: String,
        @Column("name") val name: String,
        @Column("flavor_id") val flavor_id: UUID?,
        @Column("current_weight_grams") val current_weight_grams: Int,
        @Column("total_weight_grams") val total_weight_grams: Int,
        @Column("created_at") val created_at: OffsetDateTime,
        @Column("updated_at") val updated_at: OffsetDateTime,
        @Column("updated_by") val updated_by: UUID,
    )
}
