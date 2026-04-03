package com.hookah.kek_hookah.feature.market.internal.usecase

import com.hookah.kek_hookah.feature.market.internal.repository.MarketRepository
import com.hookah.kek_hookah.feature.market.model.MarketArc
import com.hookah.kek_hookah.feature.market.model.MarketArcCreatedEvent
import com.hookah.kek_hookah.feature.market.model.MarketArcId
import com.hookah.kek_hookah.feature.market.model.MarketArcView
import com.hookah.kek_hookah.feature.market.model.MarketForCreate
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class CreateMarketCommand(
    private val repository: MarketRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
) {
    suspend fun execute(request: MarketForCreate): MarketArcView {
        require(request.name.isNotBlank()) { "name must not be blank" }
        require(request.weightGrams > 0) { "weightGrams must be > 0" }

        val arc = MarketArc(
            id = MarketArcId(),
            brandId = request.brandId,
            flavorId = request.flavorId,
            name = request.name,
            weightGrams = request.weightGrams,
            count = request.count,
            gtin = request.gtin?.takeIf { it.isNotBlank() },
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
            updatedBy = request.updatedBy,
        )
        val saved = tx.executeAndAwait { repository.insert(arc) }
        eventPublisher + MarketArcCreatedEvent(arc = saved, publishedAt = OffsetDateTime.now())
        return repository.findViewById(saved.id)!!
    }
}
