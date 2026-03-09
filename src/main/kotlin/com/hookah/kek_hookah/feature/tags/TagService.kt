package com.hookah.kek_hookah.feature.tags

import com.hookah.kek_hookah.feature.tags.internal.repository.TagRepository
import com.hookah.kek_hookah.feature.tags.internal.usecase.CreateTagCommand
import com.hookah.kek_hookah.feature.tags.internal.usecase.UpdateTagCommand
import com.hookah.kek_hookah.feature.tags.model.Tag
import com.hookah.kek_hookah.feature.tags.model.TagForCreate
import com.hookah.kek_hookah.feature.tags.model.TagForUpdate
import com.hookah.kek_hookah.feature.tags.model.TagId
import org.springframework.stereotype.Component

@Component
class TagService(
    private val repository: TagRepository,
    private val createTagCommand: CreateTagCommand,
    private val updateTagCommand: UpdateTagCommand,
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

}