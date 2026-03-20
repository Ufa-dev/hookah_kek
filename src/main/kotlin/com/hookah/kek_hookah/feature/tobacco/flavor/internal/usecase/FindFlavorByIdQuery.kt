package com.hookah.kek_hookah.feature.tobacco.flavor.internal.usecase

import com.hookah.kek_hookah.feature.tags.TagService
import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.feature.tobacco.flavor.internal.repository.FlavorRepository
import com.hookah.kek_hookah.feature.tobacco.flavor.internal.repository.FlavorsTagRepository
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.TabacoFlavor
import org.springframework.stereotype.Component

@Component
class FindFlavorByIdQuery(
    private val flavorsTagRepository: FlavorsTagRepository,
    private val flavorRepository: FlavorRepository,
    private val tagService: TagService,
) {
    suspend fun execute(id: FlavorId): TabacoFlavor? {
        val flavor = flavorRepository.findById(id)
        val tagIds = flavorsTagRepository.findAllByFlavorId(id)

        val tags = tagIds.mapNotNull { tagUuid ->
            tagService.findById(TagId(tagUuid))
        }

        return flavor?.copy(
            tags = tags
        )
    }
}