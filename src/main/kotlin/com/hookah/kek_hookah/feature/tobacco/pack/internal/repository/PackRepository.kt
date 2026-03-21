package com.hookah.kek_hookah.feature.tobacco.pack.internal.repository

import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackId
import com.hookah.kek_hookah.feature.user.model.UserId
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.*

@Component
class PackRepository(
    private val template: R2dbcEntityTemplate,
) {

    suspend fun findById(id: PackId): FlavorPack? =
        template.select(PackEntity::class.java)
            .matching(Query.query(where("id").`is`(id.id)))
            .awaitOneOrNull()
            ?.toPack()

    suspend fun findAll(limit: Int, afterId: String?): List<FlavorPack> {
        val criteria = if (!afterId.isNullOrEmpty()) where("id").greaterThan(afterId) else null
        val query = (if (criteria != null) Query.query(criteria) else Query.empty())
            .sort(Sort.by(Sort.Direction.ASC, "id"))
            .limit(limit)
        return template.select(PackEntity::class.java)
            .matching(query)
            .all()
            .collectList()
            .awaitSingle()
            .map { it.toPack() }
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
        val id: String,

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
