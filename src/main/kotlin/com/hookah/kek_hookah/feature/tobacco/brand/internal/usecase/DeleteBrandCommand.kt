package com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.brand.internal.repository.BrandRepository
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandDeletedEvent
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class DeleteBrandCommand(
    private val repository: BrandRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
) {
    suspend fun execute(id: BrandId) {
        val brand = tx.executeAndAwait {
            val b = repository.findById(id)
                ?: throw IllegalArgumentException("Brand '$id' not found")
            repository.delete(id)
            b
        }!!
        eventPublisher + BrandDeletedEvent(brand = brand, publishedAt = OffsetDateTime.now())
    }
}
