package com.hookah.kek_hookah.feature.tobacco.e2e.pack

import com.hookah.kek_hookah.feature.tobacco.e2e.auth.AuthorizedWebTestClient
import com.hookah.kek_hookah.feature.tobacco.pack.api.dto.PackForCreateDto
import com.hookah.kek_hookah.feature.tobacco.pack.api.dto.PackForUpdateDto
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.utils.crud.Slice
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

const val PACK_URL = "/api/v1/pack"

fun AuthorizedWebTestClient.createPack(
    tagId: String = UUID.randomUUID().toString(),
    name: String = "pack-${UUID.randomUUID().toString().take(8)}",
    totalWeightGrams: Int = 100,
    currentWeightGrams: Int = 50,
    flavorId: UUID? = null
): WebTestClient.ResponseSpec =
    post().uri(PACK_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            PackForCreateDto(
                tagId = tagId,
                name = name,
                totalWeightGrams = totalWeightGrams,
                currentWeightGrams = currentWeightGrams,
                flavorId = flavorId
            )
        )
        .exchange()

fun AuthorizedWebTestClient.createPackAndGet(
    tagId: String = UUID.randomUUID().toString(),
    name: String = "pack-${UUID.randomUUID().toString().take(8)}",
    totalWeightGrams: Int = 100,
    currentWeightGrams: Int = 50
): FlavorPack =
    createPack(tagId = tagId, name = name, totalWeightGrams = totalWeightGrams, currentWeightGrams = currentWeightGrams)
        .expectStatus().isOk
        .expectBody<FlavorPack>()
        .returnResult().responseBody!!

// Pack path uses String id, not UUID
fun AuthorizedWebTestClient.getPackById(id: UUID): WebTestClient.ResponseSpec =
    get().uri("$PACK_URL/$id").exchange()

fun AuthorizedWebTestClient.listPacks(limit: Int = 20): WebTestClient.ResponseSpec =
    get().uri("$PACK_URL?limit=$limit").exchange()

fun AuthorizedWebTestClient.updatePack(
    id: UUID,
    name: String,
    totalWeightGrams: Int = 100,
    currentWeightGrams: Int = 50,
    flavorId: UUID? = null
): WebTestClient.ResponseSpec =
    put().uri("$PACK_URL/$id")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            PackForUpdateDto(
                name = name,
                totalWeightGrams = totalWeightGrams,
                currentWeightGrams = currentWeightGrams,
                flavorId = flavorId
            )
        )
        .exchange()

fun AuthorizedWebTestClient.deletePack(id: UUID): WebTestClient.ResponseSpec =
    delete().uri("$PACK_URL/$id").exchange()
