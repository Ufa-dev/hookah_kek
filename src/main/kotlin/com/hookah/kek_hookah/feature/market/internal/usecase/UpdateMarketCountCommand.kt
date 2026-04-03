package com.hookah.kek_hookah.feature.market.internal.usecase

import com.hookah.kek_hookah.feature.market.internal.repository.MarketRepository
import com.hookah.kek_hookah.feature.market.model.MarketArcUpdatedEvent
import com.hookah.kek_hookah.feature.market.model.MarketArcView
import com.hookah.kek_hookah.feature.market.model.MarketForUpdateCount
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class UpdateMarketCountCommand(
    private val repository: MarketRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
) {
    suspend fun execute(request: MarketForUpdateCount): MarketArcView {
        val existing = repository.findById(request.id)
            ?: throw NoSuchElementException("Market arc '${request.id}' not found")

        require(request.count >= 0) { "count must be >= 0" }

        val updated = existing.copy(
            count = request.count,
            updatedAt = OffsetDateTime.now(),
            updatedBy = request.updatedBy,
        )
        val saved = tx.executeAndAwait { repository.update(updated) }
        eventPublisher + MarketArcUpdatedEvent(before = existing, after = saved, publishedAt = OffsetDateTime.now())
        return repository.findViewById(saved.id)!!
    }
}
