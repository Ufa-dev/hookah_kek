package com.hookah.kek_hookah.feature.tags

import com.hookah.kek_hookah.feature.tags.internal.repository.TagRepository
import com.hookah.kek_hookah.feature.tags.internal.usecase.CreateTagCommand
import com.hookah.kek_hookah.feature.tags.internal.usecase.ListTagsQuery
import com.hookah.kek_hookah.feature.tags.internal.usecase.UpdateTagCommand
import com.hookah.kek_hookah.feature.tags.model.Tag
import com.hookah.kek_hookah.feature.tags.model.TagForCreate
import com.hookah.kek_hookah.feature.tags.model.TagForUpdate
import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.utils.crud.Slice
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TagService(
    private val repository: TagRepository,
    private val createTagCommand: CreateTagCommand,
    private val updateTagCommand: UpdateTagCommand,
    private val listTagsQuery: ListTagsQuery,
) {

    suspend fun findById(id: TagId): Tag? {
        return repository.findById(id)
    }

    suspend fun findByName(name: String): Tag? {
        return repository.findByName(name)
    }

    suspend fun create(request: TagForCreate): Tag {
        return createTagCommand.execute(request)
    }

    suspend fun update(request: TagForUpdate): Tag {
        return updateTagCommand.execute(request)
    }

    suspend fun findAllByIds(ids: List<UUID>): List<Tag> {
        return repository.findAllByIds(ids)
    }

    suspend fun list(limit: Int, afterId: UUID?): Slice<Tag> {
        return listTagsQuery.execute(limit, afterId)
    }

}