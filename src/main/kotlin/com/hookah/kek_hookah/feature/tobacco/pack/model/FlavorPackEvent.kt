package com.hookah.kek_hookah.feature.tobacco.pack.model

import com.hookah.kek_hookah.infrastructure.event.Event
import java.time.OffsetDateTime

interface FlavorPackEvent : Event

data class FlavorPackCreatedEvent(
    val pack: FlavorPack,
    override val publishedAt: OffsetDateTime
) : FlavorPackEvent

data class FlavorPackUpdatedEvent(
    val before: FlavorPack,
    val after: FlavorPack,
    override val publishedAt: OffsetDateTime
) : FlavorPackEvent