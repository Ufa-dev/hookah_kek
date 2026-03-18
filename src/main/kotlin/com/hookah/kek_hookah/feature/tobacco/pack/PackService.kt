package com.hookah.kek_hookah.feature.tobacco.pack

import com.hookah.kek_hookah.feature.tobacco.pack.internal.repository.FlavorPackRepository
import com.hookah.kek_hookah.feature.tobacco.pack.internal.usecase.CreateFlavorPackCommand
import com.hookah.kek_hookah.feature.tobacco.pack.internal.usecase.UpdateFlavorPackCommand
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPackForCreate
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPackForUpdate
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackId
import org.springframework.stereotype.Component

@Component
class FlavorPackService(
    private val repository: FlavorPackRepository,
    private val createFlavorPackCommand: CreateFlavorPackCommand,
    private val updateFlavorPackCommand: UpdateFlavorPackCommand,
) {
    suspend fun findById(id: PackId): FlavorPack? {
        return repository.findById(id)
    }

    suspend fun findAll(): List<FlavorPack> {
        return repository.findAll()
    }

    suspend fun create(request: FlavorPackForCreate): FlavorPack {
        return createFlavorPackCommand.execute(request)
    }

    suspend fun update(request: FlavorPackForUpdate): FlavorPack {
        return updateFlavorPackCommand.execute(request)
    }

}