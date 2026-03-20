package com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.brand.internal.repository.BrandsTagRepository
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandTagDeleteEvent
import com.hookah.kek_hookah.feature.tobacco.brand.model.UpdateTagForBrand
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class DeleteTagFromBrandCommand(
    private val tx: TransactionalOperator,
    private val repository: BrandsTagRepository,
    private val eventPublisher: EventPublisher
) {
    suspend fun execute(request: UpdateTagForBrand) {
        val brandTag = repository.findByBrandIdAndTagId(request.brandId, request.tagId)
            ?: throw IllegalStateException("Not found tag for this brand!")

        tx.executeAndAwait { repository.delete(brandTag) }

        eventPublisher + BrandTagDeleteEvent(
            brandTag = brandTag,
            publishedAt = OffsetDateTime.now()
        )

    }

}
