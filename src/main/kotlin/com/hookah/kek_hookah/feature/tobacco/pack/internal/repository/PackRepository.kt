package com.hookah.kek_hookah.feature.tobacco.pack.internal.repository

import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackId
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackTagId
import com.hookah.kek_hookah.feature.user.model.UserId
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
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
import java.util.*

@Component
class PackRepository(
    private val template: R2dbcEntityTemplate,
    private val db: DatabaseClient,
) {

    suspend fun findById(id: PackId): FlavorPack? =
        template.select(PackEntity::class.java)
            .matching(Query.query(where("id").`is`(id.id)))
            .awaitOneOrNull()
            ?.toPack()

    suspend fun findByTagId(tagId: PackTagId): FlavorPack? =
        template.select(PackEntity::class.java)
            .matching(Query.query(where("tag_id").`is`(tagId.id)))
            .awaitOneOrNull()
            ?.toPack()

    suspend fun findAll(limit: Int, afterId: UUID?, name: String?, flavorId: UUID?, brandId: UUID?): List<FlavorPack> {
        val sql = buildString {
            append("SELECT * FROM flavor_pack WHERE 1=1")
            if (!name.isNullOrBlank()) append(" AND LOWER(name) LIKE :name")
            if (flavorId != null)      append(" AND flavor_id = :flavorId")
            if (brandId != null)       append(" AND flavor_id IN (SELECT id FROM tabacoo_flavor WHERE brand_id = :brandId)")
            if (afterId != null)       append(" AND id > :afterId")
            append(" ORDER BY id ASC LIMIT :limit")
        }
        var spec = db.sql(sql).bind("limit", limit)
        if (!name.isNullOrBlank()) spec = spec.bind("name", "%${name.lowercase()}%")
        if (flavorId != null)      spec = spec.bind("flavorId", flavorId)
        if (brandId != null)       spec = spec.bind("brandId", brandId)
        if (afterId != null)       spec = spec.bind("afterId", afterId)
        return spec.map { row, _ ->
            PackEntity(
                id = row.get("id", UUID::class.java)!!,
                tagId = row.get("tag_id", String::class.java)!!,
                name = row.get("name", String::class.java)!!,
                flavorId = row.get("flavor_id", UUID::class.java),
                currentWeightGrams = row.get("current_weight_grams", Integer::class.java)!!.toInt(),
                totalWeightGrams = row.get("total_weight_grams", Integer::class.java)!!.toInt(),
                createdAt = row.get("created_at", OffsetDateTime::class.java)!!,
                updatedAt = row.get("updated_at", OffsetDateTime::class.java)!!,
                updatedBy = row.get("updated_by", UUID::class.java)!!,
            )
        }.all().collectList().awaitSingle().map { it.toPack() }
    }

    suspend fun insert(pack: FlavorPack): FlavorPack =
        template.insert(pack.toEntity()).awaitSingle().toPack()

    suspend fun update(pack: FlavorPack): FlavorPack =
        template.update(pack.toEntity()).awaitSingle().toPack()

    suspend fun delete(id: PackId) {
        template.delete(PackEntity::class.java)
            .matching(Query.query(where("id").`is`(id.id)))
            .all()
            .awaitSingle()
    }
    private fun PackEntity.toPack() = FlavorPack(
        id = PackId(id),
        tagId = PackTagId(tagId),
        name = name,
        flavorId = flavorId?.let { FlavorId(it) },
        currentWeightGrams = currentWeightGrams,
        totalWeightGrams = totalWeightGrams,
        createdAt = createdAt,
        updatedAt = updatedAt,
        updatedBy = UserId(updatedBy),
    )

    private fun FlavorPack.toEntity() = PackEntity(
        id = id.id,
        tagId = tagId.id,
        name = name,
        flavorId = flavorId?.id,
        currentWeightGrams = currentWeightGrams,
        totalWeightGrams = totalWeightGrams,
        createdAt = createdAt,
        updatedAt = updatedAt,
        updatedBy = updatedBy.id,
    )

    @Table("flavor_pack")
    data class PackEntity(
        @Id
        @Column("id")
        val id: UUID,

        @Column("tag_id")
        val tagId: String,

        @Column("name")
        val name: String,

        @Column("flavor_id")
        val flavorId: UUID?,

        @Column("current_weight_grams")
        val currentWeightGrams: Int,

        @Column("total_weight_grams")
        val totalWeightGrams: Int,

        @Column("created_at")
        val createdAt: OffsetDateTime,

        @Column("updated_at")
        val updatedAt: OffsetDateTime,

        @Column("updated_by")
        val updatedBy: UUID,
    )
}
