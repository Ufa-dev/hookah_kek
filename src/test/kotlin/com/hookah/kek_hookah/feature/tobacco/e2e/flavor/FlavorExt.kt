package com.hookah.kek_hookah.feature.tobacco.e2e.flavor

import com.hookah.kek_hookah.feature.tobacco.e2e.auth.AuthorizedWebTestClient
import com.hookah.kek_hookah.feature.tobacco.e2e.brand.createBrandAndGet
import com.hookah.kek_hookah.feature.tobacco.flavor.api.dto.FlavorCreateDto
import com.hookah.kek_hookah.feature.tobacco.flavor.api.dto.FlavorUpdateDto
import com.hookah.kek_hookah.feature.tobacco.flavor.api.dto.UpdateTagForFlavorDto
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.TabacoFlavor
import com.hookah.kek_hookah.feature.tags.model.TagId
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

const val FLAVOR_URL = "/api/v1/flavor"

fun AuthorizedWebTestClient.createFlavor(
    brandId: UUID,
    name: String = "flavor-${UUID.randomUUID().toString().take(8)}",
    description: String? = "Test flavor",
    strength: Short = 5
): WebTestClient.ResponseSpec =
    post().uri(FLAVOR_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(FlavorCreateDto(brandId = brandId, name = name, description = description, strength = strength))
        .exchange()

// Creates a brand first if brandId not provided, then creates a flavor
fun AuthorizedWebTestClient.createFlavorAndGet(
    brandId: UUID? = null,
    name: String = "flavor-${UUID.randomUUID().toString().take(8)}",
    description: String? = "Test flavor",
    strength: Short = 5
): TabacoFlavor {
    val resolvedBrandId = brandId ?: createBrandAndGet().id.id
    return createFlavor(brandId = resolvedBrandId, name = name, description = description, strength = strength)
        .expectStatus().isOk
        .expectBody<TabacoFlavor>()
        .returnResult().responseBody!!
}

fun AuthorizedWebTestClient.getFlavorById(id: UUID): WebTestClient.ResponseSpec =
    get().uri("$FLAVOR_URL/id/$id").exchange()

fun AuthorizedWebTestClient.getFlavorsByName(name: String): WebTestClient.ResponseSpec =
    get().uri("$FLAVOR_URL/name/$name").exchange()

fun AuthorizedWebTestClient.getFlavorsByBrand(brandId: UUID): WebTestClient.ResponseSpec =
    get().uri("$FLAVOR_URL/brand/$brandId").exchange()

fun AuthorizedWebTestClient.listFlavors(): WebTestClient.ResponseSpec =
    get().uri(FLAVOR_URL).exchange()

fun AuthorizedWebTestClient.searchFlavors(brandId: UUID? = null, name: String? = null): WebTestClient.ResponseSpec {
    val params = buildList {
        if (brandId != null) add("brandId=$brandId")
        if (name != null) add("name=$name")
    }.joinToString("&")
    val uri = if (params.isNotEmpty()) "$FLAVOR_URL/search?$params" else "$FLAVOR_URL/search"
    return get().uri(uri).exchange()
}

fun AuthorizedWebTestClient.updateFlavor(
    id: UUID,
    brandId: UUID,
    name: String,
    description: String? = null,
    strength: Short = 5
): WebTestClient.ResponseSpec =
    put().uri("$FLAVOR_URL/$id")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(FlavorUpdateDto(brandId = brandId, name = name, description = description, strength = strength))
        .exchange()

fun AuthorizedWebTestClient.addTagToFlavor(flavorId: FlavorId, tagId: TagId): WebTestClient.ResponseSpec =
    patch().uri("$FLAVOR_URL/add-tag")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(UpdateTagForFlavorDto(flavorId = flavorId, tagId = tagId))
        .exchange()

fun AuthorizedWebTestClient.removeTagFromFlavor(flavorId: FlavorId, tagId: TagId): WebTestClient.ResponseSpec =
    patch().uri("$FLAVOR_URL/remove-tag")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(UpdateTagForFlavorDto(flavorId = flavorId, tagId = tagId))
        .exchange()

fun AuthorizedWebTestClient.deleteFlavor(id: UUID): WebTestClient.ResponseSpec =
    delete().uri("$FLAVOR_URL/$id").exchange()
