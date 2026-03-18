package com.hookah.kek_hookah.feature.tobacco.flavor.internal.repository

import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.TabacoFlavor
import com.hookah.kek_hookah.feature.user.model.UserId
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class FlavorRepository(
    private val template: R2dbcEntityTemplate
) {
    suspend fun findById(id: FlavorId): TabacoFlavor? {
        return template.select(TabacoFlavorEntity::class.java)
            .matching(Query.query(where("id").`is`(id.id)))
            .awaitOneOrNull()
            ?.toFlavor()
    }

    suspend fun findAll(): List<TabacoFlavor> {
        return template.select(TabacoFlavorEntity::class.java)
            .all()
            .collectList()
            .awaitSingle()
            .map { it.toFlavor() }
    }

    suspend fun findByBrandId(brandId: BrandId): List<TabacoFlavor> {
        return template.select(TabacoFlavorEntity::class.java)
            .matching(Query.query(where("brand_id").`is`(brandId.id)))
            .all()
            .collectList()
            .awaitSingle()
            .map { it.toFlavor() }
    }

    suspend fun insert(flavor: TabacoFlavor): TabacoFlavor {
        return template.insert(flavor.toEntity()).awaitSingle().toFlavor()
    }

    suspend fun update(flavor: TabacoFlavor): TabacoFlavor {
        return template.update(flavor.toEntity()).awaitSingle().toFlavor()
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

    private fun TabacoFlavorEntity.toFlavor() = TabacoFlavor(
        id = FlavorId(id),
        brandId = BrandId(brandId),
        name = name,
        description = description,
        warehouseProductId = warehouseProductId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        updatedBy = UserId(updatedBy)
    )

    private fun TabacoFlavor.toEntity() = TabacoFlavorEntity(
        id = id.id,
        brandId = brandId.id,
        name = name,
        description = description,
        warehouseProductId = warehouseProductId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        updatedBy = updatedBy.id
    )

    @Table("tabacoo_flavor")
    data class TabacoFlavorEntity(
        @Id val
        id: UUID,

        @Column("brand_id")
        val brandId: UUID,

        @Column("name")
        val name: String,

        @Column("description")
        val description: String,

        @Column("warehouse_product_id")
        val warehouseProductId: String,

        @Column("created_at")
        val createdAt: OffsetDateTime,

        @Column("updated_at")
        val updatedAt: OffsetDateTime,

        @Column("updated_by")
        val updatedBy: UUID
    )
}