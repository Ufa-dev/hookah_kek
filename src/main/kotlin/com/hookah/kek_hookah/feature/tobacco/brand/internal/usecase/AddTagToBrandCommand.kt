package com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.brand.model.AddTagForBrand
import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import org.springframework.stereotype.Component

@Component
class AddTagToBrandCommand {
    suspend fun execute(request: AddTagForBrand): TabacoBrand {
        TODO("Not yet implemented")
    }
}
