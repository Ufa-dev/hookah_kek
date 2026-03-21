package com.hookah.kek_hookah.feature.tobacco.pack.internal.usecase

import com.hookah.kek_hookah.feature.tobacco.pack.internal.repository.PackRepository
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.utils.crud.Slice
import org.springframework.stereotype.Component
import java.util.*

@Component
class ListPacksQuery(
    private val repository: PackRepository,
) {
    suspend fun execute(limit: Int, afterId: UUID?): Slice<FlavorPack> {
        val packs = repository.findAll(limit, afterId)
        val nextToken = if (packs.size == limit) packs.last().id.id.toString() else null
        return Slice(items = packs, nextToken = nextToken)
    }
}
