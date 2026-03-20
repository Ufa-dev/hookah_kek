package com.hookah.kek_hookah.feature.tobacco.pack.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.pack.internal.repository.PackRepository
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackDeletedEvent
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackId
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class DeletePackCommand(
    private val repository: PackRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
) {
    suspend fun execute(id: PackId) {
        val pack = repository.findById(id)
            ?: throw IllegalArgumentException("Pack '$id' not found")
        tx.executeAndAwait { repository.delete(id) }
        eventPublisher + PackDeletedEvent(pack = pack, publishedAt = OffsetDateTime.now())
    }
}
