package com.hookah.kek_hookah.feature.tobacco.pack.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.pack.internal.repository.PackRepository
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackUpdatedEvent
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackWeigh
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class WeighPackCommand(
    private val repository: PackRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
) {
    suspend fun execute(request: PackWeigh): FlavorPack {
        val existing = repository.findByTagId(request.tagId)
            ?: throw IllegalArgumentException("Pack with tagId='${request.tagId.id}' not found")

        require(request.currentWeightGrams >= 0) { "currentWeightGrams must be >= 0" }

        val delta = request.currentWeightGrams - existing.currentWeightGrams

        val updatedTotalWeight = when {
            delta < 0 -> existing.totalWeightGrams + delta
            else -> existing.totalWeightGrams
        }

        require(updatedTotalWeight >= 0) {
            "totalWeightGrams must not become negative"
        }

        val updated = existing.copy(
            currentWeightGrams = request.currentWeightGrams,
            totalWeightGrams = updatedTotalWeight,
            updatedAt = request.updatedAt,
            updatedBy = request.updatedBy,
        )

        return tx.executeAndAwait {
            val saved = repository.update(updated)

            repository.insertHist(
                pack = saved,
                eventType = "weigh",
            )

            saved
        }.also { saved ->
            eventPublisher + PackUpdatedEvent(
                before = existing,
                after = saved,
                publishedAt = OffsetDateTime.now(),
            )
        }
    }
}