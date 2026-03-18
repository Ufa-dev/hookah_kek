package com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase

import com.hookah.kek_hookah.feature.tags.TagService
import com.hookah.kek_hookah.feature.tags.model.Tag
import com.hookah.kek_hookah.feature.tobacco.brand.internal.repository.BrandRepository
import com.hookah.kek_hookah.feature.tobacco.brand.internal.repository.BrandsTagRepository
import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import org.springframework.stereotype.Component

@Component
class FindBrandsByTagQuery(
    private val brandsTagRepository: BrandsTagRepository,
    private val brandRepository: BrandRepository,
    private val tagService: TagService,
) {
    suspend fun execute(tags: List<Tag>): List<TabacoBrand> {
        val brandIds = brandsTagRepository.findAllBrandIdsByTagIds(tags.map { it-> it.id })

        TODO("Not yet implemented")
    }

}
