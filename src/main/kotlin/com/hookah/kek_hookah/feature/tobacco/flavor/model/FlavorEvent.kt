package com.hookah.kek_hookah.feature.tobacco.flavor.model

import com.hookah.kek_hookah.infrastructure.event.Event
import java.time.OffsetDateTime

interface FlavorEvent : Event

data class FlavorCreatedEvent(
    val flavor: TabacoFlavor,
    override val publishedAt: OffsetDateTime
) : FlavorEvent

data class FlavorUpdatedEvent(
    val before: TabacoFlavor,
    val after: TabacoFlavor,
    override val publishedAt: OffsetDateTime
) : FlavorEvent