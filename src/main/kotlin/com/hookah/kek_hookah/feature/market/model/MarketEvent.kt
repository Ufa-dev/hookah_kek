package com.hookah.kek_hookah.feature.market.model

import com.hookah.kek_hookah.infrastructure.event.Event
import java.time.OffsetDateTime

interface MarketEvent : Event

data class MarketArcCreatedEvent(
    val arc: MarketArc,
    override val publishedAt: OffsetDateTime,
) : MarketEvent

data class MarketArcUpdatedEvent(
    val before: MarketArc,
    val after: MarketArc,
    override val publishedAt: OffsetDateTime,
) : MarketEvent

data class MarketArcDeletedEvent(
    val arc: MarketArc,
    override val publishedAt: OffsetDateTime,
) : MarketEvent
