package com.hookah.kek_hookah.feature.tobacco.e2e.market

import com.hookah.kek_hookah.feature.market.api.dto.MarketCreateDto
import com.hookah.kek_hookah.feature.market.api.dto.MarketUpdateCountDto
import com.hookah.kek_hookah.feature.market.api.dto.MarketUpdateDto
import com.hookah.kek_hookah.feature.market.model.MarketArcView
import com.hookah.kek_hookah.feature.market.model.MarketTotalWeightView
import com.hookah.kek_hookah.feature.tobacco.e2e.auth.AuthorizedWebTestClient
import com.hookah.kek_hookah.feature.tobacco.e2e.brand.createBrandAndGet
import com.hookah.kek_hookah.feature.tobacco.e2e.flavor.createFlavorAndGet
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

const val MARKET_URL = "/api/v1/market"

fun AuthorizedWebTestClient.createMarket(
    brandId: UUID,
    flavorId: UUID,
    name: String = "market-${UUID.randomUUID().toString().take(8)}",
    weightGrams: Int = 100,
    count: Int = 0,
    gtin: String? = null,
): WebTestClient.ResponseSpec =
    post().uri(MARKET_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(MarketCreateDto(brandId = brandId, flavorId = flavorId, name = name, weightGrams = weightGrams, count = count, gtin = gtin))
        .exchange()

suspend fun AuthorizedWebTestClient.createMarketAndGet(
    name: String = "market-${UUID.randomUUID().toString().take(8)}",
    weightGrams: Int = 100,
    count: Int = 0,
): MarketArcView {
    val brand = createBrandAndGet()
    val flavor = createFlavorAndGet(brandId = brand.id.id)
    return createMarket(brandId = brand.id.id, flavorId = flavor.id.id, name = name, weightGrams = weightGrams, count = count)
        .expectStatus().isOk
        .expectBody<MarketArcView>()
        .returnResult().responseBody!!
}

fun AuthorizedWebTestClient.getMarketById(id: UUID): WebTestClient.ResponseSpec =
    get().uri("$MARKET_URL/$id").exchange()

fun AuthorizedWebTestClient.listMarkets(
    limit: Int = 20,
    brandName: String? = null,
    flavorName: String? = null,
    name: String? = null,
    weightMin: Int? = null,
    weightMax: Int? = null,
    countMin: Int? = null,
): WebTestClient.ResponseSpec {
    val params = buildList {
        add("limit=$limit")
        if (brandName != null)  add("brandName=$brandName")
        if (flavorName != null) add("flavorName=$flavorName")
        if (name != null)       add("name=$name")
        if (weightMin != null)  add("weightMin=$weightMin")
        if (weightMax != null)  add("weightMax=$weightMax")
        if (countMin != null)   add("countMin=$countMin")
    }.joinToString("&")
    return get().uri("$MARKET_URL?$params").exchange()
}

fun AuthorizedWebTestClient.updateMarket(
    id: UUID,
    brandId: UUID,
    flavorId: UUID,
    name: String,
    weightGrams: Int = 100,
    count: Int = 0,
    gtin: String? = null,
): WebTestClient.ResponseSpec =
    put().uri("$MARKET_URL/$id")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(MarketUpdateDto(brandId = brandId, flavorId = flavorId, name = name, weightGrams = weightGrams, count = count, gtin = gtin))
        .exchange()

fun AuthorizedWebTestClient.patchMarketCount(id: UUID, count: Int): WebTestClient.ResponseSpec =
    patch().uri("$MARKET_URL/$id/count")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(MarketUpdateCountDto(count = count))
        .exchange()

fun AuthorizedWebTestClient.getMarketTotalWeight(flavorId: UUID): WebTestClient.ResponseSpec =
    get().uri("$MARKET_URL/total-weight/$flavorId").exchange()

fun AuthorizedWebTestClient.deleteMarket(id: UUID): WebTestClient.ResponseSpec =
    delete().uri("$MARKET_URL/$id").exchange()
