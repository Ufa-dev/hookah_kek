package com.hookah.kek_hookah.feature.tobacco.pack

import com.hookah.kek_hookah.feature.tobacco.pack.internal.usecase.*
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackForCreate
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackForUpdate
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackId
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackTagId
import com.hookah.kek_hookah.feature.tobacco.pack.internal.repository.PackRepository
import com.hookah.kek_hookah.utils.crud.Slice
import org.springframework.stereotype.Component
import java.util.*

@Component
class PackService(
    private val createPackCommand: CreatePackCommand,
    private val updatePackCommand: UpdatePackCommand,
    private val deletePackCommand: DeletePackCommand,
    private val findPackByIdQuery: FindPackByIdQuery,
    private val listPacksQuery: ListPacksQuery,
    private val repository: PackRepository,
) {

    suspend fun findById(id: PackId): FlavorPack? =
        findPackByIdQuery.execute(id)

    suspend fun findByTagId(tagId: PackTagId): FlavorPack? =
        repository.findByTagId(tagId)

    suspend fun list(limit: Int, afterId: UUID?, name: String?, flavorId: UUID?, brandId: UUID?): Slice<FlavorPack> =
        listPacksQuery.execute(limit, afterId, name, flavorId, brandId)

    suspend fun create(request: PackForCreate): FlavorPack =
        createPackCommand.execute(request)

    suspend fun update(request: PackForUpdate): FlavorPack =
        updatePackCommand.execute(request)

    suspend fun delete(id: PackId) =
        deletePackCommand.execute(id)
}
