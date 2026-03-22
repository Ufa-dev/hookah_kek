package com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase

import com.hookah.kek_hookah.feature.tags.TagService
import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.feature.tobacco.brand.internal.repository.BrandRepository
import com.hookah.kek_hookah.feature.tobacco.brand.internal.repository.BrandsTagRepository
import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import com.hookah.kek_hookah.utils.crud.Slice
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ListBrandsQuery(
    private val brandRepository: BrandRepository,
    private val brandsTagRepository: BrandsTagRepository,
    private val tagService: TagService,
) {
    suspend fun execute(
        limit: Int,
        afterId: UUID?,
        tagIds: List<TagId>? = null,
        name: String? = null,
    ): Slice<TabacoBrand> {
        val allowedBrandIds: List<UUID>? = if (!tagIds.isNullOrEmpty()) {
            val ids = brandsTagRepository.findAllBrandIdsByTagIds(tagIds).map { it.id }
            // No brands have these tags — return empty result immediately
            if (ids.isEmpty()) return Slice(items = emptyList(), nextToken = null)
            ids
        } else null

        val brands = brandRepository.findAll(limit, afterId, allowedBrandIds, name)

        val enriched = brands.map { brand ->
            val tagUuids = brandsTagRepository.findAllByBrandId(brand.id)
            val tags = tagUuids.mapNotNull { tagUuid -> tagService.findById(TagId(tagUuid)) }
            brand.copy(tags = tags)
        }

        val nextToken = if (enriched.size == limit) enriched.last().id.id.toString() else null
        return Slice(items = enriched, nextToken = nextToken)
    }
}
