package com.hookah.kek_hookah.feature.market

import com.hookah.kek_hookah.feature.market.internal.repository.MarketRepository
import com.hookah.kek_hookah.feature.market.internal.usecase.CreateMarketCommand
import com.hookah.kek_hookah.feature.market.internal.usecase.DeleteMarketCommand
import com.hookah.kek_hookah.feature.market.internal.usecase.FindMarketByIdQuery
import com.hookah.kek_hookah.feature.market.internal.usecase.GetTotalWeightByFlavorQuery
import com.hookah.kek_hookah.feature.market.internal.usecase.UpdateMarketCommand
import com.hookah.kek_hookah.feature.market.internal.usecase.UpdateMarketCountCommand
import com.hookah.kek_hookah.feature.market.model.MarketArcId
import com.hookah.kek_hookah.feature.market.model.MarketArcView
import com.hookah.kek_hookah.feature.market.model.MarketForCreate
import com.hookah.kek_hookah.feature.market.model.MarketForUpdate
import com.hookah.kek_hookah.feature.market.model.MarketForUpdateCount
import com.hookah.kek_hookah.feature.market.model.MarketTotalWeightView
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class MarketService(
    private val repository: MarketRepository,
    private val createMarketCommand: CreateMarketCommand,
    private val updateMarketCommand: UpdateMarketCommand,
    private val updateMarketCountCommand: UpdateMarketCountCommand,
    private val deleteMarketCommand: DeleteMarketCommand,
    private val findMarketByIdQuery: FindMarketByIdQuery,
    private val getTotalWeightByFlavorQuery: GetTotalWeightByFlavorQuery,
) {
    suspend fun create(request: MarketForCreate): MarketArcView =
        createMarketCommand.execute(request)

    suspend fun update(request: MarketForUpdate): MarketArcView =
        updateMarketCommand.execute(request)

    suspend fun updateCount(request: MarketForUpdateCount): MarketArcView =
        updateMarketCountCommand.execute(request)

    suspend fun delete(id: MarketArcId) =
        deleteMarketCommand.execute(id)

    suspend fun findById(id: MarketArcId): MarketArcView? =
        findMarketByIdQuery.execute(id)

    suspend fun totalWeightByFlavor(flavorId: UUID): MarketTotalWeightView =
        getTotalWeightByFlavorQuery.execute(flavorId)

    suspend fun list(
        limit: Int,
        afterId: UUID? = null,
        brandName: String? = null,
        flavorName: String? = null,
        nameContains: String? = null,
        weightMin: Int? = null,
        weightMax: Int? = null,
        countMin: Int? = null,
        sortBy: String = "updated_at",
        sortDir: String = "desc",
    ): List<MarketArcView> = repository.findAllViews(
        limit = limit,
        afterId = afterId,
        brandName = brandName,
        flavorName = flavorName,
        nameContains = nameContains,
        weightMin = weightMin,
        weightMax = weightMax,
        countMin = countMin,
        sortBy = sortBy,
        sortDir = sortDir,
    )
}
