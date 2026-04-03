package com.hookah.kek_hookah.infrastructure.event

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class EventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    suspend fun publish(event: Event) {
        applicationEventPublisher.publishEvent(event)
    }

    suspend operator fun plus(event: Event): Unit = publish(event)
}
