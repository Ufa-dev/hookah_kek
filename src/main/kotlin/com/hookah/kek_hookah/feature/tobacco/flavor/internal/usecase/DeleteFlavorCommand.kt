package com.hookah.kek_hookah.feature.tobacco.flavor.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.flavor.internal.repository.FlavorRepository
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorDeletedEvent
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class DeleteFlavorCommand(
    private val repository: FlavorRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
) {
    suspend fun execute(id: FlavorId) {
        val flavor = tx.executeAndAwait {
            val f = repository.findById(id)
                ?: throw IllegalArgumentException("Flavor '$id' not found")
            repository.delete(id)
            f
        }!!
        eventPublisher + FlavorDeletedEvent(flavor = flavor, publishedAt = OffsetDateTime.now())
    }
}
