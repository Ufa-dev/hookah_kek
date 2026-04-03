package com.hookah.kek_hookah.feature.market.internal.usecase

import com.hookah.kek_hookah.feature.market.internal.repository.MarketRepository
import com.hookah.kek_hookah.feature.market.model.MarketTotalWeightView
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class GetTotalWeightByFlavorQuery(private val repository: MarketRepository) {
    suspend fun execute(flavorId: UUID): MarketTotalWeightView =
        MarketTotalWeightView(
            flavorId = flavorId,
            totalWeightGrams = repository.totalWeightByFlavor(flavorId),
        )
}
