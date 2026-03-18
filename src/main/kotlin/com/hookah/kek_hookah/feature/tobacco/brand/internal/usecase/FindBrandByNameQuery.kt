package com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase

import com.hookah.kek_hookah.feature.tags.TagService
import com.hookah.kek_hookah.feature.tags.model.Tag
import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.feature.tobacco.brand.internal.repository.BrandRepository
import com.hookah.kek_hookah.feature.tobacco.brand.internal.repository.BrandsTagRepository
import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import org.springframework.stereotype.Component

@Component
class FindBrandByNameQuery(
    private val brandsTagRepository: BrandsTagRepository,
    private val brandRepository: BrandRepository,
    private val tagService: TagService,
) {

    //todo with corutins
    suspend fun execute(name: String): List<TabacoBrand> {
        val brands = brandRepository.findAllByName(name)

        if (brands.isEmpty()) return emptyList()

        val brandsWithTags = mutableListOf<TabacoBrand>()

        for (brand in brands) {
            val tagIds = brandsTagRepository.findAllByBrandId(brand.id)

            val tags = mutableListOf<Tag>()
            for (tagId in tagIds) {
                val tag = tagService.findById(TagId(tagId))
                tag?.let { tags.add(it) }
            }

            brandsWithTags.add(brand.copy(tags = tags))
        }
        return brandsWithTags
    }
}
