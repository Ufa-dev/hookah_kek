package com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.brand.model.AddTagForBrand
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandForUpdate
import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import org.springframework.stereotype.Component

@Component
class UpdateBrandCommand {
    suspend fun execute(request: BrandForUpdate): TabacoBrand {
        TODO("Not yet implemented")
    }

}
