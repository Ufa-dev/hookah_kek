package com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.brand.internal.repository.BrandsTagRepository
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandTag
import com.hookah.kek_hookah.feature.tobacco.brand.model.UpdateTagForBrand
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Component
class AddTagToBrandCommand(
    private val tx: TransactionalOperator,
    private val repository: BrandsTagRepository,
    private val eventPublisher: EventPublisher
) {
    suspend fun execute(request: UpdateTagForBrand): BrandTag {
        repository.findByBrandIdAndTagId(request.brandId, request.tagId)
            ?.let { throw IllegalArgumentException("Brand already have this tag!") }

        return BrandTag(
            request.brandId,
            request.tagId
        ).let { brand ->
            tx.executeAndAwait { repository.insert(brand) }
        }.also {
            //todo drop event here eventPublisher
        }
    }
}
