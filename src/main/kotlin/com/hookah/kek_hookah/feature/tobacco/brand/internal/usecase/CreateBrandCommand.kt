package com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandForCreate
import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import org.springframework.stereotype.Component

@Component
class CreateBrandCommand {
    suspend fun execute(request: BrandForCreate): TabacoBrand {
        TODO("Not yet implemented")
    }

}
