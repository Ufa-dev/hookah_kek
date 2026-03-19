package com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.brand.internal.repository.BrandRepository
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandCreatedEvent
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandForCreate
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class CreateBrandCommand(
    private val repository: BrandRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
    ) {
    suspend fun execute(request: BrandForCreate): TabacoBrand {
        repository.findByName(request.name)
            ?.let { throw IllegalArgumentException("Brand with this name already exist!") }

        return TabacoBrand(
            id = BrandId(),
            name = request.name,
            description = request.description,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
            updatedBy = request.updatedBy
        ).let { brand ->
            tx.executeAndAwait { repository.insert(brand) }
        }.also { brand ->
            eventPublisher + BrandCreatedEvent(
                brand = brand,
                publishedAt = OffsetDateTime.now()
            )
        }
    }

}
