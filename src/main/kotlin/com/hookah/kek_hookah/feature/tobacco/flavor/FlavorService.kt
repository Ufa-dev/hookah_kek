package com.hookah.kek_hookah.feature.tobacco.flavor

import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.flavor.internal.repository.FlavorRepository
import com.hookah.kek_hookah.feature.tobacco.flavor.internal.usecase.CreateFlavorCommand
import com.hookah.kek_hookah.feature.tobacco.flavor.internal.usecase.UpdateFlavorCommand
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorForCreate
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorForUpdate
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.TabacoFlavor
import org.springframework.stereotype.Component

@Component
class FlavorService(
    private val repository: FlavorRepository,
    private val createFlavorCommand: CreateFlavorCommand,
    private val updateFlavorCommand: UpdateFlavorCommand,
) {
    suspend fun findById(id: FlavorId): TabacoFlavor?
    {
        return repository.findById(id)
    }

    suspend fun findAll(): List<TabacoFlavor> {
        return repository.findAll()
    }

    suspend fun findByBrandId(brandId: BrandId): List<TabacoFlavor> {
        return repository.findByBrandId(brandId)
    }

    suspend fun create(request: FlavorForCreate): TabacoFlavor {
        return createFlavorCommand.execute(request)
    }

    suspend fun update(request: FlavorForUpdate): TabacoFlavor {
        return updateFlavorCommand.execute(request)
    }

}