package com.hookah.kek_hookah.feature.tobacco.pack.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.pack.internal.repository.FlavorPackRepository
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPackForUpdate
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPackUpdatedEvent
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class UpdateFlavorPackCommand(
    private val repository: FlavorPackRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
) {
    suspend fun execute(request: FlavorPackForUpdate): FlavorPack {
        val existing = repository.findById(request.id)
            ?: throw IllegalArgumentException("Flavor pack not found")

        if (request.currentWeightGrams > request.totalWeightGrams) {
            throw IllegalArgumentException("Current weight cannot exceed total weight")
        }

        val updated = existing.copy(
            flavorId = request.flavorId,
            currentWeightGrams = request.currentWeightGrams,
            totalWeightGrams = request.totalWeightGrams,
            updatedAt = OffsetDateTime.now(),
            updatedBy = request.userId
        )
        return tx.executeAndAwait {
            repository.update(updated)
        }.also {
            eventPublisher + FlavorPackUpdatedEvent(
                before = existing,
                after = it,
                publishedAt = OffsetDateTime.now()
            )
        }
    }
}