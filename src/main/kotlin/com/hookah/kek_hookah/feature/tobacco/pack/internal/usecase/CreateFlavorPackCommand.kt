package com.hookah.kek_hookah.feature.tobacco.pack.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.pack.internal.repository.FlavorPackRepository
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPackCreatedEvent
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPackForCreate
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class CreateFlavorPackCommand(
    private val repository: FlavorPackRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
) {
    suspend fun execute(request: FlavorPackForCreate): FlavorPack {
        repository.findById(request.id)?.let {
            throw IllegalArgumentException("Flavor pack with id '${request.id.value}' already exists")
        }


        val pack = FlavorPack(
            id = request.id,
            flavorId = request.flavorId,
            currentWeightGrams = request.currentWeightGrams,
            totalWeightGrams = request.totalWeightGrams,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
            updatedBy = request.userId
        )
        return tx.executeAndAwait {
            repository.insert(pack)
        }.also {
            eventPublisher + FlavorPackCreatedEvent(
                pack = it,
                publishedAt = OffsetDateTime.now()
            )
        }
    }
}