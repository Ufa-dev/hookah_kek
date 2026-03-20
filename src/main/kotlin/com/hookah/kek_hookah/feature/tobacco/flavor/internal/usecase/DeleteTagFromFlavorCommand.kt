package com.hookah.kek_hookah.feature.tobacco.flavor.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.flavor.internal.repository.FlavorsTagRepository
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorTagDeleteEvent
import com.hookah.kek_hookah.feature.tobacco.flavor.model.UpdateTagForFlavor
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class DeleteTagFromFlavorCommand(
    private val tx: TransactionalOperator,
    private val repository: FlavorsTagRepository,
    private val eventPublisher: EventPublisher
) {
    suspend fun execute(request: UpdateTagForFlavor) {
        val flavorTag = repository.findByFlavorIdAndTagId(request.flavorId, request.tagId)
            ?: throw IllegalArgumentException("Flavor does not have this tag!")

        tx.executeAndAwait {
            repository.delete(flavorTag)
        }
        eventPublisher + FlavorTagDeleteEvent(
            flavorTag = flavorTag,
            publishedAt = OffsetDateTime.now()
        )
    }
}
