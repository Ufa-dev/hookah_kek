package com.hookah.kek_hookah.feature.tobacco.brand

import com.hookah.kek_hookah.feature.tags.TagService
import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.feature.tobacco.brand.internal.repository.BrandsTagRepository
import com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase.*
import com.hookah.kek_hookah.feature.tobacco.brand.model.*
import com.hookah.kek_hookah.utils.crud.Slice
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BrandService(
    private val deleteTagFromBrandCommand: DeleteTagFromBrandCommand,
    private val addTagToBrandCommand: AddTagToBrandCommand,
    private val createTagCommand: CreateBrandCommand,
    private val updateTagCommand: UpdateBrandCommand,
    private val findBrandByNameQuery: FindBrandByNameQuery,
    private val findBrandByIdQuery: FindBrandByIdQuery,
    private val listBrandsQuery: ListBrandsQuery,
    private val brandsTagRepository: BrandsTagRepository,
    private val tagService: TagService,
    private val deleteBrandCommand: DeleteBrandCommand,
) {

    suspend fun findById(id: BrandId): TabacoBrand? {
        return findBrandByIdQuery.execute(id)
    }

    suspend fun findAllByName(name: String): List<TabacoBrand> {
        return findBrandByNameQuery.execute(name)
    }

    suspend fun findAllByTag(tags: List<TagId>): List<TabacoBrand> {
        val brandIds = brandsTagRepository.findAllBrandIdsByTagIds(tags)
        return brandIds.mapNotNull { findById(it) }
    }

    suspend fun addTag(request: UpdateTagForBrand): TabacoBrand {
        findById(request.brandId)
            ?: throw IllegalArgumentException("Brand with id ${request.brandId} not found")

        tagService.findById(request.tagId)
            ?: throw IllegalArgumentException("Tag with id ${request.tagId} not found")

        addTagToBrandCommand.execute(request)

        return findById(request.brandId)
            ?: throw IllegalStateException("Brand disappeared after adding tag!")
    }

    suspend fun deleteTag(request: UpdateTagForBrand): TabacoBrand {
        findById(request.brandId)
            ?: throw IllegalArgumentException("Brand with id ${request.brandId} not found")

        tagService.findById(request.tagId)
            ?: throw IllegalArgumentException("Tag with id ${request.tagId} not found")

        deleteTagFromBrandCommand.execute(request)

        return findById(request.brandId)
            ?: throw IllegalStateException("Brand disappeared after adding tag!")
    }

    suspend fun create(request: BrandForCreate): TabacoBrand {
        return createTagCommand.execute(request)
    }

    suspend fun update(request: BrandForUpdate): TabacoBrand {
        return updateTagCommand.execute(request)
    }

    suspend fun list(limit: Int, afterId: UUID?): Slice<TabacoBrand> {
        return listBrandsQuery.execute(limit, afterId)
    }

    suspend fun delete(id: BrandId) {
        deleteBrandCommand.execute(id)
    }
}
