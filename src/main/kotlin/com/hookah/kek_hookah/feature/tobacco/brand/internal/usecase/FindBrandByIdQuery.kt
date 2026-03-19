package com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase

import com.hookah.kek_hookah.feature.tags.TagService
import com.hookah.kek_hookah.feature.tags.model.Tag
import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.feature.tobacco.brand.internal.repository.BrandRepository
import com.hookah.kek_hookah.feature.tobacco.brand.internal.repository.BrandsTagRepository
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import org.springframework.stereotype.Component

@Component
class FindBrandByIdQuery(
    private val brandsTagRepository: BrandsTagRepository,
    private val brandRepository: BrandRepository,
    private val tagService: TagService,
) {
    suspend fun execute(id: BrandId): TabacoBrand? {
        val brand = brandRepository.findById(id)
        val tagIds = brandsTagRepository.findAllByBrandId(id)

        val tags: List<Tag> = tagIds.mapNotNull { tagUuid ->
            tagService.findById(TagId(tagUuid))
        }

        return brand?.copy(
            tags = tags
        )
    }

}
