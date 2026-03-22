package com.hookah.kek_hookah.feature.tobacco.e2e.brand

import com.hookah.kek_hookah.feature.tobacco.brand.api.dto.BrandForCreateDto
import com.hookah.kek_hookah.feature.tobacco.brand.api.dto.BrandForUpdateDto
import com.hookah.kek_hookah.feature.tobacco.brand.api.dto.UpdateTagForBrandDto
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.AuthorizedWebTestClient
import com.hookah.kek_hookah.utils.crud.Slice
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

const val BRAND_URL = "/api/v1/brand"

fun AuthorizedWebTestClient.createBrand(
    name: String = "brand-${UUID.randomUUID().toString().take(8)}",
    description: String = "Test brand description"
): WebTestClient.ResponseSpec =
    post().uri(BRAND_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(BrandForCreateDto(name = name, description = description))
        .exchange()

fun AuthorizedWebTestClient.createBrandAndGet(
    name: String = "brand-${UUID.randomUUID().toString().take(8)}",
    description: String = "Test brand description"
): TabacoBrand =
    createBrand(name, description)
        .expectStatus().isOk
        .expectBody<TabacoBrand>()
        .returnResult().responseBody!!

fun AuthorizedWebTestClient.getBrandById(id: UUID): WebTestClient.ResponseSpec =
    get().uri("$BRAND_URL/id/$id").exchange()

fun AuthorizedWebTestClient.getBrandByName(name: String): WebTestClient.ResponseSpec =
    get().uri("$BRAND_URL/name/$name").exchange()

fun AuthorizedWebTestClient.listBrands(limit: Int = 20, after: UUID? = null): WebTestClient.ResponseSpec {
    val uri = if (after != null) "$BRAND_URL?limit=$limit&after=$after" else "$BRAND_URL?limit=$limit"
    return get().uri(uri).exchange()
}

fun AuthorizedWebTestClient.getBrandsByTags(tagIds: List<UUID>): WebTestClient.ResponseSpec {
    val query = tagIds.joinToString("&") { "tags=$it" }
    return get().uri("$BRAND_URL/brands?$query").exchange()
}

fun AuthorizedWebTestClient.updateBrand(id: UUID, name: String, description: String? = "Updated description"): WebTestClient.ResponseSpec =
    put().uri("$BRAND_URL/$id")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(BrandForUpdateDto(name = name, description = description))
        .exchange()

fun AuthorizedWebTestClient.addTagToBrand(brandId: BrandId, tagId: TagId): WebTestClient.ResponseSpec =
    patch().uri("$BRAND_URL/add-tag")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(UpdateTagForBrandDto(tagId = tagId, brandId = brandId))
        .exchange()

fun AuthorizedWebTestClient.removeTagFromBrand(brandId: BrandId, tagId: TagId): WebTestClient.ResponseSpec =
    patch().uri("$BRAND_URL/remove-tag")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(UpdateTagForBrandDto(tagId = tagId, brandId = brandId))
        .exchange()

fun AuthorizedWebTestClient.deleteBrand(id: UUID): WebTestClient.ResponseSpec =
    delete().uri("$BRAND_URL/$id").exchange()
