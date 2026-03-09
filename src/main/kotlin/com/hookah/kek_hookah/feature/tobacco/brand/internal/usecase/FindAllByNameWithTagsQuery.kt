package com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import org.springframework.stereotype.Component

@Component
class FindAllByNameWithTagsQuery {
    suspend fun execute(name: String): List<TabacoBrand> {
        TODO("Not yet implemented")
    }

}
