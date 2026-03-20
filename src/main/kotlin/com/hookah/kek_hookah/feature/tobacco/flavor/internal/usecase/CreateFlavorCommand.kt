package com.hookah.kek_hookah.feature.tobacco.flavor.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.flavor.internal.repository.FlavorRepository
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorCreatedEvent
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorForCreate
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.TabacoFlavor
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class CreateFlavorCommand(
    private val repository: FlavorRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
) {
    suspend fun execute(request: FlavorForCreate): TabacoFlavor {
        repository.findByBrandAndName(request.brandId, request.name)?.let {
            throw IllegalArgumentException("Flavor with name '${request.name}' already exists for this brand")
        }

        val flavor = TabacoFlavor(
            id = FlavorId(),
            brandId = request.brandId,
            name = request.name,
            description = request.description,
            strength = request.strength,
            //warehouseProductId = request.warehouseProductId,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
            updatedBy = request.userId
        )
        return tx.executeAndAwait {
            repository.insert(flavor)
        }.also {
            eventPublisher + FlavorCreatedEvent(
                flavor = it,
                publishedAt = OffsetDateTime.now()
            )
        }
    }
}