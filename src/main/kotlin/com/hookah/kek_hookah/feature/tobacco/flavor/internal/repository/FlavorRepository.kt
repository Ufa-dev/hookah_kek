package com.hookah.kek_hookah.feature.tobacco.flavor.internal.repository

import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.TabacoFlavor
import com.hookah.kek_hookah.feature.user.model.UserId
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.domain.Sort
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.*

@Component
class FlavorRepository(
    private val template: R2dbcEntityTemplate,
    private val db: DatabaseClient,
) {

    suspend fun findById(id: FlavorId): TabacoFlavor? {
        return template.select(TabacoFlavorEntity::class.java)
            .matching(Query.query(where("id").`is`(id.id)))
            .awaitOneOrNull()
            ?.toFlavor()
    }

    suspend fun findAll(cursor: FlavorId?, limit: Int): List<TabacoFlavor> {
        var criteria: Criteria = where("id").isNotNull
        if (cursor != null) {
            criteria = criteria.and("id").greaterThan(cursor.id)
        }
        val query = Query.query(criteria)
            .sort(Sort.by(Sort.Order.asc("id")))
            .limit(limit)

        return template.select(TabacoFlavorEntity::class.java)
            .matching(query)
            .all()
            .collectList()
            .awaitSingle()
            .map { it.toFlavor() }
    }

    suspend fun findByBrandId(brandId: BrandId, cursor: FlavorId?, limit: Int): List<TabacoFlavor> {
        var criteria: Criteria = where("brand_id").`is`(brandId.id)
        if (cursor != null) {
            criteria = criteria.and("id").greaterThan(cursor.id)
        }
        val query = Query.query(criteria)
            .sort(Sort.by(Sort.Order.asc("id")))
            .limit(limit)

        return template.select(TabacoFlavorEntity::class.java)
            .matching(query)
            .all()
            .collectList()
            .awaitSingle()
            .map { it.toFlavor() }
    }

    suspend fun findAllByName(name: String, cursor: FlavorId?, limit: Int): List<TabacoFlavor> {
        val sql = buildString {
            append("SELECT * FROM tabacoo_flavor WHERE LOWER(name) LIKE :name")
            if (cursor != null) append(" AND id > :cursor")
            append(" ORDER BY id ASC LIMIT :limit")
        }
        var spec = db.sql(sql)
            .bind("name", "%${name.lowercase()}%")
            .bind("limit", limit)
        if (cursor != null) spec = spec.bind("cursor", cursor.id)
        return spec.map { row, meta ->
            TabacoFlavorEntity(
                id = row.get("id", UUID::class.java)!!,
                brandId = row.get("brand_id", UUID::class.java)!!,
                name = row.get("name", String::class.java)!!,
                description = row.get("description", String::class.java),
                strength = row.get("strength", java.lang.Short::class.java)?.toShort(),
                createdAt = row.get("created_at", OffsetDateTime::class.java)!!,
                updatedAt = row.get("updated_at", OffsetDateTime::class.java)!!,
                updatedBy = row.get("updated_by", UUID::class.java)!!,
            )
        }.all().collectList().awaitSingle().map { it.toFlavor() }
    }

    suspend fun findByBrandIdAndNameContaining(brandId: BrandId, name: String, cursor: FlavorId?, limit: Int): List<TabacoFlavor> {
        val sql = buildString {
            append("SELECT * FROM tabacoo_flavor WHERE brand_id = :brandId AND LOWER(name) LIKE :name")
            if (cursor != null) append(" AND id > :cursor")
            append(" ORDER BY id ASC LIMIT :limit")
        }
        var spec = db.sql(sql)
            .bind("brandId", brandId.id)
            .bind("name", "%${name.lowercase()}%")
            .bind("limit", limit)
        if (cursor != null) spec = spec.bind("cursor", cursor.id)
        return spec.map { row, meta ->
            TabacoFlavorEntity(
                id = row.get("id", UUID::class.java)!!,
                brandId = row.get("brand_id", UUID::class.java)!!,
                name = row.get("name", String::class.java)!!,
                description = row.get("description", String::class.java),
                strength = row.get("strength", java.lang.Short::class.java)?.toShort(),
                createdAt = row.get("created_at", OffsetDateTime::class.java)!!,
                updatedAt = row.get("updated_at", OffsetDateTime::class.java)!!,
                updatedBy = row.get("updated_by", UUID::class.java)!!,
            )
        }.all().collectList().awaitSingle().map { it.toFlavor() }
    }

    suspend fun findByBrandAndName(brandId: BrandId, name: String): TabacoFlavor? {
        return template.select(TabacoFlavorEntity::class.java)
            .matching(
                Query.query(
                    where("brand_id").`is`(brandId.id)
                        .and("name").`is`(name)
                )
            )
            .awaitOneOrNull()
            ?.toFlavor()
    }

    suspend fun existsByBrandIdAndName(brandId: BrandId, name: String): Boolean {
        return template.select(TabacoFlavorEntity::class.java)
            .matching(
                Query.query(
                    where("brand_id").`is`(brandId.id)
                        .and("name").`is`(name)
                )
            )
            .exists()
            .awaitSingle()
    }

    suspend fun insert(flavor: TabacoFlavor): TabacoFlavor {
        return template.insert(flavor.toEntity()).awaitSingle().toFlavor()
    }

    suspend fun update(flavor: TabacoFlavor): TabacoFlavor {
        return template.update(flavor.toEntity()).awaitSingle().toFlavor()
    }

    suspend fun delete(id: FlavorId) {
        template.delete(TabacoFlavorEntity::class.java)
            .matching(Query.query(where("id").`is`(id.id)))
            .all()
            .awaitSingle()
    }

    private fun TabacoFlavorEntity.toFlavor() = TabacoFlavor(
        id = FlavorId(id),
        brandId = BrandId(brandId),
        name = name,
        description = description,
        strength = strength,
        createdAt = createdAt,
        updatedAt = updatedAt,
        updatedBy = UserId(updatedBy)
    )

    private fun TabacoFlavor.toEntity() = TabacoFlavorEntity(
        id = id.id,
        brandId = brandId.id,
        name = name,
        description = description,
        strength = strength,
        createdAt = createdAt,
        updatedAt = updatedAt,
        updatedBy = updatedBy.id
    )

    @Table("tabacoo_flavor")
    data class TabacoFlavorEntity(
        @Id
        val id: UUID,

        @Column("brand_id")
        val brandId: UUID,

        @Column("name")
        val name: String,

        @Column("description")
        val description: String?,

        @Column("strength")
        val strength: Short?,

        @Column("created_at")
        val createdAt: OffsetDateTime,

        @Column("updated_at")
        val updatedAt: OffsetDateTime,

        @Column("updated_by")
        val updatedBy: UUID
    )
}