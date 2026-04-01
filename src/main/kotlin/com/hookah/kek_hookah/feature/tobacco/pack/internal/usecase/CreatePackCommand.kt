package com.hookah.kek_hookah.feature.tobacco.pack.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.pack.internal.repository.PackRepository
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackCreatedEvent
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackForCreate
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackId
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackTagId
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class CreatePackCommand(
    private val repository: PackRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
) {
    suspend fun execute(request: PackForCreate): FlavorPack {
        repository.findByTagId(PackTagId(request.tagId))
            ?.let { throw IllegalArgumentException("Pack with tagId '${request.tagId}' already exists") }

        require(request.totalWeightGrams > 0) { "totalWeightGrams must be > 0" }
        require(request.currentWeightGrams >= 0) { "currentWeightGrams must be >= 0" }

        val pack = FlavorPack(
            id = PackId(),
            tagId = PackTagId(request.tagId),
            name = request.name,
            flavorId = request.flavorId,
            currentWeightGrams = request.currentWeightGrams,
            totalWeightGrams = request.totalWeightGrams,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
            updatedBy = request.updatedBy,
        )
        return tx.executeAndAwait { repository.insert(pack) }
            .also { saved ->
                eventPublisher + PackCreatedEvent(pack = saved, publishedAt = OffsetDateTime.now())
            }
    }
}