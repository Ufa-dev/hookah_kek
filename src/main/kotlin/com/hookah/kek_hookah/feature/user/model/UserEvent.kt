package com.hookah.kek_hookah.feature.user.model

import com.hookah.kek_hookah.infrastructure.event.Event
import java.time.OffsetDateTime

interface UserEvent : Event

data class UserCreatedEvent(
    val user: User,
    override val publishedAt: OffsetDateTime
) : UserEvent

data class UserUpdatedEvent(
    val before: User,
    val after: User,
    override val publishedAt: OffsetDateTime
) : UserEvent
