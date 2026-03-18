package com.hookah.kek_hookah.feature.tobacco.brand.model

import com.hookah.kek_hookah.infrastructure.event.Event
import java.time.OffsetDateTime

interface BrandEvent : Event

data class BrandCreatedEvent(
    val brand: TabacoBrand,
    override val publishedAt: OffsetDateTime
) : BrandEvent

data class BrandUpdatedEvent(
    val before: TabacoBrand,
    val after: TabacoBrand,
    override val publishedAt: OffsetDateTime
) : BrandEvent