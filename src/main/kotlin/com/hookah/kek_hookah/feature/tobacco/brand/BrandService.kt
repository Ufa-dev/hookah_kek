package com.hookah.kek_hookah.feature.tobacco.brand

import com.hookah.kek_hookah.feature.tobacco.brand.internal.BrandRepository
import com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase.*
import com.hookah.kek_hookah.feature.tobacco.brand.model.*
import org.springframework.stereotype.Component

@Component
class BrandService(
    private val repository: BrandRepository,
    private val findAllByNameWithTagsQuery: FindAllByNameWithTagsQuery,
    private val addTagToBrandCommand: AddTagToBrandCommand,
    private val findByIdWithTagsQuery: FindByIdWithTagsQuery,
    private val createTagCommand: CreateBrandCommand,
    private val updateTagCommand: UpdateBrandCommand,
    private val findByTagQuery: FindByTagQuery,
) {

    suspend fun findById(id: BrandId): TabacoBrand? {
        return repository.findById(id)
    }

    suspend fun findByIdWithTags(id: BrandId): TabacoBrand? {
        return findByIdWithTagsQuery.execute(id)
    }

    suspend fun findAllByName(name: String): List<TabacoBrand> {
        return repository.findAllByName(name)
    }

    suspend fun findAllByNameWithTags(name: String): List<TabacoBrand> {
        return findAllByNameWithTagsQuery.execute(name)
    }

    suspend fun findAllByTag(tag: String): List<TabacoBrand> {
        return findByTagQuery.execute(tag)
    }

    suspend fun addTag(request: AddTagForBrand): TabacoBrand {
        return addTagToBrandCommand.execute(request)
    }

    suspend fun create(rebrauest: BrandForCreate): TabacoBrand {
        return createTagCommand.execute(request)
    }

    suspend fun update(request: BrandForUpdate): TabacoBrand {
        return updateTagCommand.execute(request)
    }
}
