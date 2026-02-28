package com.hookah.kek_hookah.infrastructure.event

import java.time.OffsetDateTime

interface Event {
    val publishedAt: OffsetDateTime
}
