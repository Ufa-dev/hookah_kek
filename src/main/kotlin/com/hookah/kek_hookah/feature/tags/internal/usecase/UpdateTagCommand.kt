package com.hookah.kek_hookah.feature.tags.internal.usecase

import com.hookah.kek_hookah.feature.tags.internal.repository.TagRepository
import com.hookah.kek_hookah.feature.tags.model.Tag
import com.hookah.kek_hookah.feature.tags.model.TagForUpdate
import com.hookah.kek_hookah.feature.tags.model.TagUpdatedEvent
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class UpdateTagCommand(
    private val repository: TagRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
) {

    suspend fun execute(request: TagForUpdate): Tag {
        val existing = repository.findById(request.tagId)
            ?: throw IllegalArgumentException("Tag not found!")

        val updated = existing.copy(
            name = request.name,
            updatedAt = OffsetDateTime.now(),
            updatedBy = request.userId
        )

        return tx.executeAndAwait {
            repository.update(updated)
        }.also { user ->
            eventPublisher + TagUpdatedEvent(
                before = existing,
                after = user,
                publishedAt = OffsetDateTime.now()
            )
        }
    }

}
