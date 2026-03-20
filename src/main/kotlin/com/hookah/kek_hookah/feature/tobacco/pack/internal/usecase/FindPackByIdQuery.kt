package com.hookah.kek_hookah.feature.tobacco.pack.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.pack.internal.repository.PackRepository
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackId
import org.springframework.stereotype.Component

@Component
class FindPackByIdQuery(
    private val repository: PackRepository,
) {
    suspend fun execute(id: PackId): FlavorPack? = repository.findById(id)
}
