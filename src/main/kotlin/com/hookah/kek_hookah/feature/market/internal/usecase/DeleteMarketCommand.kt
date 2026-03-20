package com.hookah.kek_hookah.feature.market.internal.usecase

import com.hookah.kek_hookah.feature.market.internal.repository.MarketRepository
import com.hookah.kek_hookah.feature.market.model.MarketArcDeletedEvent
import com.hookah.kek_hookah.feature.market.model.MarketArcId
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class DeleteMarketCommand(
    private val repository: MarketRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
) {
    suspend fun execute(id: MarketArcId) {
        val arc = repository.findById(id)
            ?: throw IllegalArgumentException("Market arc '$id' not found")
        tx.executeAndAwait { repository.delete(id) }
        eventPublisher + MarketArcDeletedEvent(arc = arc, publishedAt = OffsetDateTime.now())
    }
}
