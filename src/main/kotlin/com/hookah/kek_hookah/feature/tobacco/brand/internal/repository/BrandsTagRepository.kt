package com.hookah.kek_hookah.feature.tobacco.brand.internal.repository

import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandTag
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
class BrandsTagRepository(
    private val template: R2dbcEntityTemplate

) {
    suspend fun delete(brandTag: BrandTag) {
        template.delete(BrandTagEntity::class.java)
            .matching(
                Query.query(
                    where("tabacoo_brand_id").`is`(brandTag.brandId.id)
                        .and(where("tag_id").`is`(brandTag.tagId.id))
                )
            )
            .all()
            .awaitSingle()
    }

    suspend fun insert(brandTag: BrandTag): BrandTag {
        return template.insert(brandTag.toEntity()).awaitSingle().toBrandTag()
    }

    suspend fun findByBrandIdAndTagId(brandId: BrandId, tagId: TagId): BrandTag? {
        return template.select(BrandTagEntity::class.java)
            .matching(
                Query.query(
                    where("tabacoo_brand_id").`is`(brandId.id)
                        .and(where("tag_id").`is`(tagId.id))
                )
            )
            .awaitOneOrNull()
            ?.toBrandTag()
    }

    suspend fun findAllByBrandId(id: BrandId): List<UUID> {
        return template.select(BrandTagEntity::class.java)
            .matching(
                Query.query(
                    where("tabacoo_brand_id").`is`(id.id)
                )
            )
            .all()
            .collectList()
            .awaitSingle()
            .map { it.tagId }
    }

    /**
     * Находит все brand_id по списку tag_id
     */
    suspend fun findAllBrandIdsByTagIds(tagIds: List<TagId>): List<BrandId> {
        if (tagIds.isEmpty()) return emptyList()

        return template.select(BrandTagEntity::class.java)
            .matching(
                Query.query(
                    where("tag_id").`in`(tagIds.map { it.id })
                )
            )
            .all()
            .collectList()
            .awaitSingle()
            .map { BrandId(it.brandId) }
            .distinct()
    }

    private fun BrandTagEntity.toBrandTag() = BrandTag(
        brandId = BrandId(brandId),
        tagId = TagId(tagId),
    )

    private fun BrandTag.toEntity() = BrandTagEntity(
        brandId = brandId.id,
        tagId = tagId.id,
    )

    @Table("tabacoo_brand_tags")
    data class BrandTagEntity(
        @Column("tabacoo_brand_id")
        val brandId: UUID,

        @Column("tag_id")
        val tagId: UUID,
    )

}
