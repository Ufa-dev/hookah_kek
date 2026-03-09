package com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import org.springframework.stereotype.Component

@Component
class FindByIdWithTagsQuery {
    suspend fun execute(id: BrandId): TabacoBrand? {
        TODO("Not yet implemented")
    }

}
