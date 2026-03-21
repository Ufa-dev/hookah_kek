package com.hookah.kek_hookah.feature.tobacco.flavor.internal.usecase

import com.hookah.kek_hookah.feature.tags.TagService
import com.hookah.kek_hookah.feature.tobacco.flavor.internal.repository.FlavorsTagRepository
import com.hookah.kek_hookah.feature.tobacco.flavor.model.TabacoFlavor
import org.springframework.stereotype.Component

@Component
class EnrichFlavorsWithTagsQuery(
    private val flavorsTagRepository: FlavorsTagRepository,
    private val tagService: TagService,
) {
    suspend fun execute(flavors: List<TabacoFlavor>): List<TabacoFlavor> {
        if (flavors.isEmpty()) return flavors
        val tagIdsByFlavorId = flavorsTagRepository.findAllTagIdsByFlavorIds(flavors.map { it.id })
        val allTagUuids = tagIdsByFlavorId.values.flatten().distinct()
        val tagsById = tagService.findAllByIds(allTagUuids).associateBy { it.id.id }
        return flavors.map { flavor ->
            val tags = (tagIdsByFlavorId[flavor.id.id] ?: emptyList()).mapNotNull { tagsById[it] }
            flavor.copy(tags = tags)
        }
    }
}
