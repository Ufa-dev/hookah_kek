package com.hookah.kek_hookah.feature.tobacco.e2e.tags

import com.hookah.kek_hookah.feature.tags.api.dto.TagNameForUpdateDto
import com.hookah.kek_hookah.feature.tags.model.Tag
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.AuthorizedWebTestClient
import com.hookah.kek_hookah.utils.crud.Slice
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

const val TAG_URL = "/api/v1/tag"

fun AuthorizedWebTestClient.createTag(name: String): WebTestClient.ResponseSpec =
    post().uri(TAG_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(TagNameForUpdateDto(name = name))
        .exchange()

fun AuthorizedWebTestClient.createTagAndGet(name: String): Tag =
    createTag(name)
        .expectStatus().isOk
        .expectBody<Tag>()
        .returnResult().responseBody!!

fun AuthorizedWebTestClient.getTagById(id: UUID): WebTestClient.ResponseSpec =
    get().uri("$TAG_URL/id/$id").exchange()

fun AuthorizedWebTestClient.getTagByName(name: String): WebTestClient.ResponseSpec =
    get().uri("$TAG_URL/name/$name").exchange()

fun AuthorizedWebTestClient.listTags(limit: Int = 20, after: UUID? = null): WebTestClient.ResponseSpec {
    val uri = if (after != null) "$TAG_URL?limit=$limit&after=$after" else "$TAG_URL?limit=$limit"
    return get().uri(uri).exchange()
}

fun AuthorizedWebTestClient.updateTagName(id: UUID, name: String): WebTestClient.ResponseSpec =
    patch().uri("$TAG_URL/$id/name")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(TagNameForUpdateDto(name = name))
        .exchange()
