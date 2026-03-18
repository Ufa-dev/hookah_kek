package com.hookah.kek_hookah.feature.tobacco.brand.internal.repository

import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
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
    suspend fun findAllBrandIdsByTagIds(tagIds: List<TagId>): List<UUID> {
        if (tagIds.isEmpty()) return emptyList()

        return template.select(BrandTagEntity::class.java)
            .matching(
                Query.query(
                    where("tag_id").`in`(tagIds)
                )
            )
            .all()
            .collectList()
            .awaitSingle()
            .map { it.brandId }
            .distinct()
    }



    @Table("tabacoo_brand_tags")
    data class BrandTagEntity(
        @Column("tabacoo_brand_id")
        val brandId: UUID,

        @Column("tag_id")
        val tagId: UUID,
    )

}
