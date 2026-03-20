package com.hookah.kek_hookah.feature.market.internal.usecase

import com.hookah.kek_hookah.feature.market.internal.repository.MarketRepository
import com.hookah.kek_hookah.feature.market.model.MarketArcUpdatedEvent
import com.hookah.kek_hookah.feature.market.model.MarketArcView
import com.hookah.kek_hookah.feature.market.model.MarketForUpdate
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class UpdateMarketCommand(
    private val repository: MarketRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
) {
    suspend fun execute(request: MarketForUpdate): MarketArcView {
        val existing = repository.findById(request.id)
            ?: throw IllegalArgumentException("Market arc '${request.id}' not found")

        require(request.name.isNotBlank()) { "name must not be blank" }
        require(request.weightGrams > 0) { "weightGrams must be > 0" }

        val updated = existing.copy(
            brandId = request.brandId,
            flavorId = request.flavorId,
            name = request.name,
            weightGrams = request.weightGrams,
            gtin = request.gtin?.takeIf { it.isNotBlank() },
            updatedAt = OffsetDateTime.now(),
            updatedBy = request.updatedBy,
        )
        val saved = tx.executeAndAwait { repository.update(updated) }
        eventPublisher + MarketArcUpdatedEvent(before = existing, after = saved, publishedAt = OffsetDateTime.now())
        return repository.findViewById(saved.id)!!
    }
}
