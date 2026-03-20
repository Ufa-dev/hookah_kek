package com.hookah.kek_hookah.feature.tobacco.flavor.internal.usecase

import com.hookah.kek_hookah.feature.tags.TagService
import com.hookah.kek_hookah.feature.tags.model.Tag
import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import com.hookah.kek_hookah.feature.tobacco.flavor.internal.repository.FlavorRepository
import com.hookah.kek_hookah.feature.tobacco.flavor.internal.repository.FlavorsTagRepository
import com.hookah.kek_hookah.feature.tobacco.flavor.model.TabacoFlavor
import org.springframework.stereotype.Component


@Component
class FindFlavorByNameQuery(
    private val flavorsTagRepository: FlavorsTagRepository,
    private val flavorRepository: FlavorRepository,
    private val tagService: TagService,
) {
    //todo corutins
    suspend fun execute(name: String): List<TabacoFlavor> {
        val flavors = flavorRepository.findAllByName(name)
        if (flavors.isEmpty()) return emptyList()

        val flavorsWithTags = mutableListOf<TabacoFlavor>()

        for (flavor in flavors) {
            val tagIds = flavorsTagRepository.findAllByFlavorId(flavor.id)

            val tags = mutableListOf<Tag>()
            for (tagId in tagIds) {
                val tag = tagService.findById(TagId(tagId))
                tag?.let { tags.add(it) }
            }

            flavorsWithTags.add(flavor.copy(tags = tags))
        }
        return flavorsWithTags

    }
}