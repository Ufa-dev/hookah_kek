package com.hookah.kek_hookah.feature.tobacco.flavor

import com.hookah.kek_hookah.feature.tags.TagService
import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.flavor.internal.repository.FlavorRepository
import com.hookah.kek_hookah.feature.tobacco.flavor.internal.repository.FlavorsTagRepository
import com.hookah.kek_hookah.feature.tobacco.flavor.internal.usecase.*
import com.hookah.kek_hookah.feature.tobacco.flavor.model.*
import org.springframework.stereotype.Component

@Component
class FlavorService(
    private val repository: FlavorRepository,
    private val flavorsTagRepository: FlavorsTagRepository,
    private val tagService: TagService,
    private val createFlavorCommand: CreateFlavorCommand,
    private val updateFlavorCommand: UpdateFlavorCommand,
    private val addTagToFlavorCommand: AddTagToFlavorCommand,
    private val deleteTagFromFlavorCommand: DeleteTagFromFlavorCommand,
    private val findFlavorByIdQuery: FindFlavorByIdQuery,
    private val deleteFlavorCommand: DeleteFlavorCommand,
) {
    suspend fun findById(id: FlavorId): TabacoFlavor? {
        return findFlavorByIdQuery.execute(id)
    }

    suspend fun findAll(cursor: FlavorId?, limit: Int): List<TabacoFlavor> {
        return enrichWithTags(repository.findAll(cursor, limit))
    }

    suspend fun findByBrandId(brandId: BrandId, cursor: FlavorId?, limit: Int): List<TabacoFlavor> {
        return enrichWithTags(repository.findByBrandId(brandId, cursor, limit))
    }

    suspend fun findAllByName(name: String, cursor: FlavorId?, limit: Int): List<TabacoFlavor> {
        return enrichWithTags(repository.findAllByName(name, cursor, limit))
    }

    suspend fun findByBrandIdAndNameContaining(
        brandId: BrandId,
        name: String,
        cursor: FlavorId?,
        limit: Int
    ): List<TabacoFlavor> {
        return enrichWithTags(repository.findByBrandIdAndNameContaining(brandId, name, cursor, limit))
    }

    private suspend fun enrichWithTags(flavors: List<TabacoFlavor>): List<TabacoFlavor> {
        if (flavors.isEmpty()) return flavors
        val tagIdsByFlavorId = flavorsTagRepository.findAllTagIdsByFlavorIds(flavors.map { it.id })
        val allTagUuids = tagIdsByFlavorId.values.flatten().distinct()
        val tagsById = tagService.findAllByIds(allTagUuids).associateBy { it.id.id }
        return flavors.map { flavor ->
            val tags = (tagIdsByFlavorId[flavor.id.id] ?: emptyList()).mapNotNull { tagsById[it] }
            flavor.copy(tags = tags)
        }
    }

    suspend fun create(request: FlavorForCreate): TabacoFlavor {
        return createFlavorCommand.execute(request)
    }

    suspend fun update(request: FlavorForUpdate): TabacoFlavor {
        return updateFlavorCommand.execute(request)
    }


    suspend fun findAllByTag(tags: List<TagId>, cursor: FlavorId?, limited: Int): List<TabacoFlavor> {
        val flavorIds = flavorsTagRepository.findAllFlavorIdsByTagIds(tags)
        return flavorIds.mapNotNull { findById(it) }
    }

    suspend fun addTag(request: UpdateTagForFlavor): TabacoFlavor {
        findById(request.flavorId)
            ?: throw IllegalArgumentException("Flavor with id ${request.flavorId} not found")

        tagService.findById(request.tagId)
            ?: throw IllegalArgumentException("Tag with id ${request.tagId} not found")

        addTagToFlavorCommand.execute(request)

        return findById(request.flavorId)
            ?: throw IllegalStateException("Flavor disappeared after adding tag!")
    }

    suspend fun deleteTag(request: UpdateTagForFlavor): TabacoFlavor {
        findById(request.flavorId)
            ?: throw IllegalArgumentException("Flavor with id ${request.flavorId} not found")

        tagService.findById(request.tagId)
            ?: throw IllegalArgumentException("Tag with id ${request.tagId} not found")

        deleteTagFromFlavorCommand.execute(request)

        return findById(request.flavorId)
            ?: throw IllegalStateException("Flavor disappeared after deleting tag!")
    }

    suspend fun delete(id: FlavorId) {
        deleteFlavorCommand.execute(id)
    }
}