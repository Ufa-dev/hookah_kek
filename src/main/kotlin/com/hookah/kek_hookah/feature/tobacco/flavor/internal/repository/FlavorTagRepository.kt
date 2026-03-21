package com.hookah.kek_hookah.feature.tobacco.flavor.internal.repository

import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorTag
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Component
import java.util.*

@Component
class FlavorsTagRepository(
    private val template: R2dbcEntityTemplate
) {

    suspend fun delete(flavorTag: FlavorTag) {
        template.delete(flavorTag.toEntity())
            .awaitSingle()
    }

    suspend fun insert(flavorTag: FlavorTag): FlavorTag {
        return template.insert(flavorTag.toEntity()).awaitSingle().toFlavorTag()
    }

    suspend fun findByFlavorIdAndTagId(flavorId: FlavorId, tagId: TagId): FlavorTag? {
        return template.select(FlavorTagEntity::class.java)
            .matching(
                Query.query(
                    where("tabacoo_flavor_id").`is`(flavorId.id)
                        .and("tag_id").`is`(tagId.id)
                )
            )
            .awaitOneOrNull()
            ?.toFlavorTag()
    }

    suspend fun findAllByFlavorId(flavorId: FlavorId): List<UUID> {
        return template.select(FlavorTagEntity::class.java)
            .matching(
                Query.query(
                    where("tabacoo_flavor_id").`is`(flavorId.id)
                )
            )
            .all()
            .collectList()
            .awaitSingle()
            .map { it.tagId }
    }

    suspend fun findAllTagIdsByFlavorIds(flavorIds: List<FlavorId>): Map<UUID, List<UUID>> {
        if (flavorIds.isEmpty()) return emptyMap()
        return template.select(FlavorTagEntity::class.java)
            .matching(
                Query.query(
                    where("tabacoo_flavor_id").`in`(flavorIds.map { it.id })
                )
            )
            .all()
            .collectList()
            .awaitSingle()
            .groupBy({ it.flavorId }, { it.tagId })
    }

    suspend fun findAllFlavorIdsByTagIds(tagIds: List<TagId>): List<FlavorId> {
        if (tagIds.isEmpty()) return emptyList()

        return template.select(FlavorTagEntity::class.java)
            .matching(
                Query.query(
                    where("tag_id").`in`(tagIds.map { it.id })
                )
            )
            .all()
            .collectList()
            .awaitSingle()
            .map { FlavorId(it.flavorId) }
            .distinct()
    }

    private fun FlavorTagEntity.toFlavorTag() = FlavorTag(
        flavorId = FlavorId(flavorId),
        tagId = TagId(tagId)
    )

    private fun FlavorTag.toEntity() = FlavorTagEntity(
        flavorId = flavorId.id,
        tagId = tagId.id
    )

    @Table("flavor_tags")
    data class FlavorTagEntity(
        @Column("tabacoo_flavor_id")
        val flavorId: UUID,

        @Column("tag_id")
        val tagId: UUID
    )
}