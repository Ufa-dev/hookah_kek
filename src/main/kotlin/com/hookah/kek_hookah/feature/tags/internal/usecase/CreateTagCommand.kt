package com.hookah.kek_hookah.feature.tags.internal.usecase

import com.hookah.kek_hookah.feature.tags.internal.repository.TagRepository
import com.hookah.kek_hookah.feature.tags.model.Tag
import com.hookah.kek_hookah.feature.tags.model.TagCreatedEvent
import com.hookah.kek_hookah.feature.tags.model.TagForCreate
import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class CreateTagCommand(
    private val repository: TagRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
) {
    suspend fun execute(request: TagForCreate): Tag {
        repository.findByName(request.name)
            ?.let { throw IllegalArgumentException("Tag with this name already exist!") }

        return Tag(
            id = TagId(),
            name = request.name,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
            updatedBy = request.userId
        ).let { tag ->
            tx.executeAndAwait { repository.insert(tag) }
        }.also { tag ->
            eventPublisher + TagCreatedEvent(
                tag = tag,
                publishedAt = OffsetDateTime.now()
            )
        }
    }

}
