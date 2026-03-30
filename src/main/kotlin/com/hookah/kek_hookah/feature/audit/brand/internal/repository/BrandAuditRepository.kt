package com.hookah.kek_hookah.feature.audit.brand.internal.repository

import com.hookah.kek_hookah.feature.audit.brand.model.BrandAuditRecord
import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.user.model.UserId
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class BrandAuditRepository(private val template: R2dbcEntityTemplate) {

    suspend fun insert(record: BrandAuditRecord): BrandAuditRecord =
        template.insert(record.toEntity()).awaitSingle().toRecord()

    suspend fun findAll(
        limit: Int,
        afterId: UUID?,
        userId: UUID?,
        eventType: AuditEventType?,
        entityId: UUID?,
    ): List<BrandAuditRecord> {
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
            val f = where("tabacoo_brand_id").`is`(entityId)
            criteria = criteria?.and(f) ?: f
        }

        val query = if (criteria != null) Query.query(criteria) else Query.empty()
        return template.select(BrandAuditEntity::class.java)
            .matching(query.sort(Sort.by(Sort.Direction.ASC, "id")).limit(limit))
            .all()
            .collectList()
            .awaitSingle()
            .map { it.toRecord() }
    }

    private fun BrandAuditRecord.toEntity() = BrandAuditEntity(
        id = id,
        tabacoo_brand_id = brandId.id,
        event_type = eventType.name,
        name = name,
        description = description,
        created_at = createdAt,
        updated_at = updatedAt,
        updated_by = updatedBy.id,
    )

    private fun BrandAuditEntity.toRecord() = BrandAuditRecord(
        id = id,
        brandId = BrandId(tabacoo_brand_id),
        eventType = AuditEventType.valueOf(event_type),
        name = name,
        description = description,
        updatedBy = UserId(updated_by),
        createdAt = created_at,
        updatedAt = updated_at,
    )

    @Table("tabacoo_brand_hist")
    data class BrandAuditEntity(
        @Id val id: UUID,
        @Column("tabacoo_brand_id") val tabacoo_brand_id: UUID,
        @Column("event_type") val event_type: String,
        @Column("name") val name: String,
        @Column("description") val description: String?,
        @Column("created_at") val created_at: OffsetDateTime,
        @Column("updated_at") val updated_at: OffsetDateTime,
        @Column("updated_by") val updated_by: UUID,
    )
}
