package com.hookah.kek_hookah.feature.audit.market.internal.repository

import com.hookah.kek_hookah.feature.audit.market.model.MarketAuditRecord
import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.market.model.MarketArcId
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
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
class MarketAuditRepository(private val template: R2dbcEntityTemplate) {

    suspend fun insert(record: MarketAuditRecord): MarketAuditRecord =
        template.insert(record.toEntity()).awaitSingle().toRecord()

    suspend fun findAll(
        limit: Int,
        afterId: UUID?,
        userId: UUID?,
        eventType: AuditEventType?,
        entityId: UUID?,
    ): List<MarketAuditRecord> {
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
            val f = where("market_arc_id").`is`(entityId)
            criteria = criteria?.and(f) ?: f
        }

        val query = if (criteria != null) Query.query(criteria) else Query.empty()
        return template.select(MarketAuditEntity::class.java)
            .matching(query.sort(Sort.by(Sort.Direction.ASC, "id")).limit(limit))
            .all()
            .collectList()
            .awaitSingle()
            .map { it.toRecord() }
    }

    private fun MarketAuditRecord.toEntity() = MarketAuditEntity(
        id = id,
        market_arc_id = marketArcId.id,
        event_type = eventType.name,
        brand_id = brandId.id,
        tabacoo_flavor_id = flavorId.id,
        name = name,
        weight_grams = weightGrams,
        count = count,
        gtin = gtin,
        created_at = createdAt,
        updated_at = updatedAt,
        updated_by = updatedBy.id,
    )

    private fun MarketAuditEntity.toRecord() = MarketAuditRecord(
        id = id,
        marketArcId = MarketArcId(market_arc_id),
        eventType = AuditEventType.valueOf(event_type),
        brandId = BrandId(brand_id),
        flavorId = FlavorId(tabacoo_flavor_id),
        name = name,
        weightGrams = weight_grams,
        count = count,
        gtin = gtin,
        updatedBy = UserId(updated_by),
        createdAt = created_at,
        updatedAt = updated_at,
    )

    @Table("market_arc_hist")
    data class MarketAuditEntity(
        @Id val id: UUID,
        @Column("market_arc_id")      val market_arc_id: UUID,
        @Column("event_type")         val event_type: String,
        @Column("brand_id")           val brand_id: UUID,
        @Column("tabacoo_flavor_id")  val tabacoo_flavor_id: UUID,
        @Column("name")               val name: String,
        @Column("weight_grams")       val weight_grams: Int,
        @Column("count")              val count: Int,
        @Column("gtin")               val gtin: String?,
        @Column("created_at")         val created_at: OffsetDateTime,
        @Column("updated_at")         val updated_at: OffsetDateTime,
        @Column("updated_by")         val updated_by: UUID,
    )
}
