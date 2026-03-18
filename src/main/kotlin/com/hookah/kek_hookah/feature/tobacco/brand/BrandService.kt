package com.hookah.kek_hookah.feature.tobacco.brand

import com.hookah.kek_hookah.feature.tags.model.Tag
import com.hookah.kek_hookah.feature.tobacco.brand.internal.repository.BrandRepository
import com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase.*
import com.hookah.kek_hookah.feature.tobacco.brand.model.*
import org.springframework.stereotype.Component

@Component
class BrandService(
    private val addTagToBrandCommand: AddTagToBrandCommand,
    private val findBrandByNameQuery: FindBrandByNameQuery,
    private val findBrandByIdQuery: FindBrandByIdQuery,
    private val createTagCommand: CreateBrandCommand,
    private val updateTagCommand: UpdateBrandCommand,
    private val findBrandsByTagQuery: FindBrandsByTagQuery,
    private val repository: BrandRepository,
) {

    suspend fun findById(id: BrandId): TabacoBrand? {
        return findBrandByIdQuery.execute(id)
    }

    suspend fun findAllByName(name: String): List<TabacoBrand> {
        return findBrandByNameQuery.execute(name)
    }

    suspend fun findAllByTag(tags: List<Tag>): List<TabacoBrand> {
        return findBrandsByTagQuery.execute(tags)
    }

    suspend fun addTag(request: AddTagForBrand): TabacoBrand {
        return addTagToBrandCommand.execute(request)
    }

    suspend fun create(request: BrandForCreate): TabacoBrand {
        return createTagCommand.execute(request)
    }

    suspend fun update(request: BrandForUpdate): TabacoBrand {
        return updateTagCommand.execute(request)
    }
}
