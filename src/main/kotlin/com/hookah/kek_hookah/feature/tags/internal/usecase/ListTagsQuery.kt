package com.hookah.kek_hookah.feature.tags.internal.usecase

import com.hookah.kek_hookah.feature.tags.internal.repository.TagRepository
import com.hookah.kek_hookah.feature.tags.model.Tag
import com.hookah.kek_hookah.utils.crud.Slice
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ListTagsQuery(
    private val repository: TagRepository,
) {
    suspend fun execute(limit: Int, afterId: UUID?): Slice<Tag> {
        val tags = repository.findAll(limit, afterId)
        val nextToken = if (tags.size == limit) tags.last().id.id.toString() else null
        return Slice(items = tags, nextToken = nextToken)
    }
}
