package com.hookah.kek_hookah.feature.market.internal.repository

import com.hookah.kek_hookah.feature.market.model.MarketArc
import com.hookah.kek_hookah.feature.market.model.MarketArcId
import com.hookah.kek_hookah.feature.market.model.MarketArcView
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.user.model.UserId
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class MarketRepository(
    private val template: R2dbcEntityTemplate,
    private val db: DatabaseClient,
) {

    suspend fun findById(id: MarketArcId): MarketArc? =
        template.select(MarketEntity::class.java)
            .matching(Query.query(where("id").`is`(id.id)))
            .awaitOneOrNull()
            ?.toArc()

    suspend fun findViewById(id: MarketArcId): MarketArcView? =
        db.sql(VIEW_QUERY + " WHERE m.id = :id")
            .bind("id", id.id)
            .map { row, _ -> row.toView() }
            .one()
            .awaitFirstOrNull()

    suspend fun findAllViews(limit: Int, afterId: UUID?): List<MarketArcView> {
        val sql = VIEW_QUERY +
            (if (afterId != null) " WHERE m.id > :afterId" else "") +
            " ORDER BY m.id ASC LIMIT :limit"
        var spec = db.sql(sql).bind("limit", limit)
        if (afterId != null) spec = spec.bind("afterId", afterId)
        return spec
            .map { row, _ -> row.toView() }
            .all()
            .collectList()
            .awaitSingle()
    }

    suspend fun insert(arc: MarketArc): MarketArc =
        template.insert(arc.toEntity()).awaitSingle().toArc()

    suspend fun update(arc: MarketArc): MarketArc =
        template.update(arc.toEntity()).awaitSingle().toArc()

    suspend fun delete(id: MarketArcId) {
        template.delete(MarketEntity::class.java)
            .matching(Query.query(where("id").`is`(id.id)))
            .all()
            .awaitSingle()
    }

    // ─── Mapping ──────────────────────────────────────────────────────────────

    private fun io.r2dbc.spi.Row.toView() = MarketArcView(
        id = MarketArcId(get("id", UUID::class.java)!!),
        brandId = BrandId(get("brand_id", UUID::class.java)!!),
        brandName = get("brand_name", String::class.java)!!,
        flavorId = FlavorId(get("flavor_id", UUID::class.java)!!),
        flavorName = get("flavor_name", String::class.java)!!,
        name = get("name", String::class.java)!!,
        weightGrams = get("weight_grams", Integer::class.java)!!.toInt(),
        gtin = get("gtin", String::class.java),
        createdAt = get("created_at", OffsetDateTime::class.java)!!,
        updatedAt = get("updated_at", OffsetDateTime::class.java)!!,
        updatedBy = UserId(get("updated_by", UUID::class.java)!!),
    )

    private fun MarketEntity.toArc() = MarketArc(
        id = MarketArcId(id),
        brandId = BrandId(brandId),
        flavorId = FlavorId(flavorId),
        name = name,
        weightGrams = weightGrams,
        gtin = gtin,
        createdAt = createdAt,
        updatedAt = updatedAt,
        updatedBy = UserId(updatedBy),
    )

    private fun MarketArc.toEntity() = MarketEntity(
        id = id.id,
        brandId = brandId.id,
        flavorId = flavorId.id,
        name = name,
        weightGrams = weightGrams,
        gtin = gtin,
        createdAt = createdAt,
        updatedAt = updatedAt,
        updatedBy = updatedBy.id,
    )

    @Table("market_arc")
    data class MarketEntity(
        @Id @Column("id") val id: UUID,
        @Column("brand_id") val brandId: UUID,
        @Column("tabacoo_flavor_id") val flavorId: UUID,
        @Column("name") val name: String,
        @Column("weight_grams") val weightGrams: Int,
        @Column("gtin") val gtin: String?,
        @Column("created_at") val createdAt: OffsetDateTime,
        @Column("updated_at") val updatedAt: OffsetDateTime,
        @Column("updated_by") val updatedBy: UUID,
    )

    companion object {
        private const val VIEW_QUERY = """
            SELECT m.id, m.brand_id, b.name AS brand_name,
                   m.tabacoo_flavor_id AS flavor_id, f.name AS flavor_name,
                   m.name, m.weight_grams, m.gtin,
                   m.created_at, m.updated_at, m.updated_by
            FROM market_arc m
            JOIN tabacoo_brand b ON b.id = m.brand_id
            JOIN tabacoo_flavor f ON f.id = m.tabacoo_flavor_id
        """
    }
}
