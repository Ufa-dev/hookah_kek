package com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.brand.internal.repository.BrandRepository
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandForUpdate
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandUpdatedEvent
import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class UpdateBrandCommand(
    private val repository: BrandRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
) {
    suspend fun execute(request: BrandForUpdate): TabacoBrand {
        val existing = repository.findById(request.brandId)
            ?: throw IllegalArgumentException("Brand not found")

        // Проверка уникальности имени, если оно изменилось
        if (existing.name != request.name) {
            repository.findByName(request.name)?.let {
                throw IllegalArgumentException("Brand with name '${request.name}' already exists")
            }
        }

        val updated = existing.copy(
            name = request.name,
            description = request.description,
            strength = request.strength,
            updatedAt = OffsetDateTime.now(),
            updatedBy = request.userId
        )
        return tx.executeAndAwait {
            repository.update(updated)
        }.also {
            eventPublisher + BrandUpdatedEvent(
                before = existing,
                after = it,
                publishedAt = OffsetDateTime.now()
            )
        }
    }
}