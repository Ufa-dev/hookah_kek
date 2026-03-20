package com.hookah.kek_hookah.feature.tobacco.flavor.model

import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandEvent
import com.hookah.kek_hookah.infrastructure.event.Event
import java.time.OffsetDateTime

interface FlavorTagEvent : Event

data class FlavorTagCreatedEvent(
    val flavorTag: FlavorTag,
    override val publishedAt: OffsetDateTime
) : BrandEvent

data class FlavorTagDeleteEvent(
    val flavorTag: FlavorTag,
    override val publishedAt: OffsetDateTime
) : BrandEvent