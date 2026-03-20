package com.hookah.kek_hookah.feature.tobacco.flavor.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.flavor.internal.repository.FlavorRepository
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorForUpdate
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorUpdatedEvent
import com.hookah.kek_hookah.feature.tobacco.flavor.model.TabacoFlavor
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class UpdateFlavorCommand(
    private val repository: FlavorRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
) {
    suspend fun execute(request: FlavorForUpdate): TabacoFlavor {
        val existing = repository.findById(request.flavorId)
            ?: throw IllegalArgumentException("Flavor not found")

        if (existing.brandId != request.brandId || existing.name != request.name) {
            repository.findByBrandAndName(request.brandId, request.name)?.let {
                throw IllegalArgumentException("Flavor with name '${request.name}' already exists for this brand")
            }
        }

        val updated = existing.copy(
            brandId = request.brandId,
            name = request.name,
            description = request.description,
            //warehouseProductId = request.warehouseProductId,
            updatedAt = OffsetDateTime.now(),
            updatedBy = request.userId
        )
        return tx.executeAndAwait {
            repository.update(updated)
        }.also {
            eventPublisher + FlavorUpdatedEvent(
                before = existing,
                after = it,
                publishedAt = OffsetDateTime.now()
            )
        }
    }
}