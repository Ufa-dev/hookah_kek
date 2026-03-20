package com.hookah.kek_hookah.feature.tobacco.pack.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.pack.internal.repository.PackRepository
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackForUpdate
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackUpdatedEvent
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class UpdatePackCommand(
    private val repository: PackRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
) {
    suspend fun execute(request: PackForUpdate): FlavorPack {
        val existing = repository.findById(request.id)
            ?: throw IllegalArgumentException("Pack '${request.id}' not found")

        require(request.totalWeightGrams > 0) { "totalWeightGrams must be > 0" }
        require(request.currentWeightGrams >= 0) { "currentWeightGrams must be >= 0" }
        require(request.currentWeightGrams <= request.totalWeightGrams) {
            "currentWeightGrams must not exceed totalWeightGrams"
        }

        val updated = existing.copy(
            name = request.name,
            flavorId = request.flavorId,
            currentWeightGrams = request.currentWeightGrams,
            totalWeightGrams = request.totalWeightGrams,
            updatedAt = OffsetDateTime.now(),
            updatedBy = request.updatedBy,
        )
        return tx.executeAndAwait { repository.update(updated) }
            .also { saved ->
                eventPublisher + PackUpdatedEvent(before = existing, after = saved, publishedAt = OffsetDateTime.now())
            }
    }
}
