package com.hookah.kek_hookah.feature.tobacco.pack.model

import com.hookah.kek_hookah.infrastructure.event.Event
import java.time.OffsetDateTime

interface PackEvent : Event

data class PackCreatedEvent(
    val pack: FlavorPack,
    override val publishedAt: OffsetDateTime,
) : PackEvent

data class PackUpdatedEvent(
    val before: FlavorPack,
    val after: FlavorPack,
    override val publishedAt: OffsetDateTime,
) : PackEvent

data class PackDeletedEvent(
    val pack: FlavorPack,
    override val publishedAt: OffsetDateTime,
) : PackEvent
