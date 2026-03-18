package com.hookah.kek_hookah.feature.tobacco.pack.internal.repository

import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackId
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
class FlavorPackRepository(
    private val template: R2dbcEntityTemplate
) {
    suspend fun findById(id: PackId): FlavorPack? {
        return template.select(FlavorPackEntity::class.java)
            .matching(Query.query(where("id").`is`(id.value)))
            .awaitOneOrNull()
            ?.toPack()
    }

    suspend fun findAll(): List<FlavorPack> {
        return template.select(FlavorPackEntity::class.java)
            .all()
            .collectList()
            .awaitSingle()
            .map { it.toPack() }
    }

    suspend fun insert(pack: FlavorPack): FlavorPack {
        return template.insert(pack.toEntity()).awaitSingle().toPack()
    }

    suspend fun update(pack: FlavorPack): FlavorPack {
        return template.update(pack.toEntity()).awaitSingle().toPack()
    }

    private fun FlavorPackEntity.toPack() = FlavorPack(
        id = PackId(id),
        flavorId = FlavorId(flavorId),
        currentWeightGrams = currentWeightGrams,
        totalWeightGrams = totalWeightGrams,
        createdAt = createdAt,
        updatedAt = updatedAt,
        updatedBy = UserId(updatedBy)
    )

    private fun FlavorPack.toEntity() = FlavorPackEntity(
        id = id.value,
        flavorId = flavorId.id,
        currentWeightGrams = currentWeightGrams,
        totalWeightGrams = totalWeightGrams,
        createdAt = createdAt,
        updatedAt = updatedAt,
        updatedBy = updatedBy.id
    )

    @Table("flavor_pack")
    data class FlavorPackEntity(
        @Id val
        id: String,

        @Column("flavor_id")
        val flavorId: UUID,

        @Column("current_weight_grams")
        val currentWeightGrams: Long,

        @Column("total_weight_grams")
        val totalWeightGrams: Long,

        @Column("created_at")
        val createdAt: OffsetDateTime,

        @Column("updated_at")
        val updatedAt: OffsetDateTime,

        @Column("updated_by")
        val updatedBy: UUID
    )
}