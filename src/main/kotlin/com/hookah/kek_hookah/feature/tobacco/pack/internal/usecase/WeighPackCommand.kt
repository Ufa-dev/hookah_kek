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
        require(request.currentWeightGrams <= existing.totalWeightGrams) {
            "currentWeightGrams must not exceed totalWeightGrams"
        }

        val updated = existing.copy(
            currentWeightGrams = request.currentWeightGrams,
            updatedAt = request.updatedAt,
            updatedBy = request.updatedBy,
        )

        return tx.executeAndAwait { repository.update(updated) }
            .also { saved ->
                eventPublisher + PackUpdatedEvent(
                    before = existing,
                    after = saved,
                    publishedAt = OffsetDateTime.now(),
                )
            }
    }
}