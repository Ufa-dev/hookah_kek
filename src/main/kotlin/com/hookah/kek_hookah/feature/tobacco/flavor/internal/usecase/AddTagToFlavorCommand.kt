package com.hookah.kek_hookah.feature.tobacco.flavor.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.flavor.internal.repository.FlavorsTagRepository
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorTag
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorTagCreatedEvent
import com.hookah.kek_hookah.feature.tobacco.flavor.model.UpdateTagForFlavor
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class AddTagToFlavorCommand(
    private val tx: TransactionalOperator,
    private val repository: FlavorsTagRepository,
    private val eventPublisher: EventPublisher
) {
    suspend fun execute(request: UpdateTagForFlavor): FlavorTag {
        repository.findByFlavorIdAndTagId(request.flavorId, request.tagId)
            ?.let { throw IllegalArgumentException("Flavor already has this tag!") }

        return FlavorTag(
            request.flavorId,
            request.tagId
        ).let { flavorTag ->
            tx.executeAndAwait { repository.insert(flavorTag) }
        }.also { flavorTag ->
            eventPublisher + FlavorTagCreatedEvent(
                flavorTag = flavorTag,
                publishedAt = OffsetDateTime.now()
            )
        }
    }
}