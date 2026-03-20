package com.hookah.kek_hookah.feature.market.internal.usecase

import com.hookah.kek_hookah.feature.market.internal.repository.MarketRepository
import com.hookah.kek_hookah.feature.market.model.MarketArcId
import com.hookah.kek_hookah.feature.market.model.MarketArcView
import org.springframework.stereotype.Component

@Component
class FindMarketByIdQuery(
    private val repository: MarketRepository,
) {
    suspend fun execute(id: MarketArcId): MarketArcView? =
        repository.findViewById(id)
}
