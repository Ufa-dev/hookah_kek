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

data class BrandTagAddedEvent(
    val brandId: BrandId,
    val tagId: com.hookah.kek_hookah.feature.tags.model.TagId,
    override val publishedAt: OffsetDateTime
) : BrandEvent

data class BrandTagRemovedEvent(
    val brandId: BrandId,
    val tagId: com.hookah.kek_hookah.feature.tags.model.TagId,
    override val publishedAt: OffsetDateTime
) : BrandEvent

data class BrandDeletedEvent(
    val brand: TabacoBrand,
    override val publishedAt: OffsetDateTime
) : BrandEvent
