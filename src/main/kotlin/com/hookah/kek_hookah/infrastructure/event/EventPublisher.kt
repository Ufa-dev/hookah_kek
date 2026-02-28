package com.hookah.kek_hookah.infrastructure.event

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class EventPublisher {

    private val log = LoggerFactory.getLogger(EventPublisher::class.java)

    suspend fun publish(event: Event) {
        log.info("Publishing event: {}", event)
        // we're not doing anything here yet,
        // because it's not clear how to manage it with Reactor/coroutines
    }

    suspend operator fun plus(event: Event): Unit = publish(event)

}

