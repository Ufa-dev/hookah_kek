package com.hookah.kek_hookah.feature.tobacco.brand.model

import com.hookah.kek_hookah.infrastructure.event.Event
import java.time.OffsetDateTime

interface BrandTagEvent : Event

    data class BrandTagCreatedEvent(
        val brandTag: BrandTag,
        override val publishedAt: OffsetDateTime
    ) : BrandEvent

    data class BrandTagDeleteEvent(
        val brandTag: BrandTag,
        override val publishedAt: OffsetDateTime
    ) : BrandEvent
