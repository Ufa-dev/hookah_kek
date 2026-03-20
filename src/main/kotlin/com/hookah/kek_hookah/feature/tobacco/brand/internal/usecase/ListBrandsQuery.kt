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
    suspend fun execute(limit: Int, afterId: UUID?): Slice<TabacoBrand> {
        val brands = brandRepository.findAll(limit, afterId)

        val enriched = brands.map { brand ->
            val tagIds = brandsTagRepository.findAllByBrandId(brand.id)
            val tags = tagIds.mapNotNull { tagUuid -> tagService.findById(TagId(tagUuid)) }
            brand.copy(tags = tags)
        }

        val nextToken = if (enriched.size == limit) enriched.last().id.id.toString() else null
        return Slice(items = enriched, nextToken = nextToken)
    }
}
