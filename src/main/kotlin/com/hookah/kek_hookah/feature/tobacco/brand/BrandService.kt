package com.hookah.kek_hookah.feature.tobacco.brand

import com.hookah.kek_hookah.feature.tobacco.brand.internal.repository.BrandRepository
import com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase.CreateBrandCommand
import com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase.UpdateBrandCommand
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandForCreate
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandForUpdate
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import org.springframework.stereotype.Component

@Component
class BrandService(
    private val repository: BrandRepository,
    private val createBrandCommand: CreateBrandCommand,
    private val updateBrandCommand: UpdateBrandCommand,
) {
    suspend fun findById(id: BrandId): TabacoBrand? {
       return repository.findById(id)
    }

    suspend fun findAll(): List<TabacoBrand> {
        return repository.findAll()
    }

    suspend fun create(request: BrandForCreate): TabacoBrand{
        return createBrandCommand.execute(request)
    }

    suspend fun update(request: BrandForUpdate): TabacoBrand {
       return updateBrandCommand.execute(request)
    }
}