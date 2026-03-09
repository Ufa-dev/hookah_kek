package com.hookah.kek_hookah.feature.tags.model

import com.hookah.kek_hookah.infrastructure.event.Event
import java.time.OffsetDateTime

interface TagEvent : Event

data class TagCreatedEvent(
    val tag: Tag,
    override val publishedAt: OffsetDateTime
) : TagEvent

data class TagUpdatedEvent(
    val before: Tag,
    val after: Tag,
    override val publishedAt: OffsetDateTime
) : TagEvent
