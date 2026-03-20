package com.hookah.kek_hookah.api

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

class BrandApiIT : BaseApiIT() {

    private lateinit var token: String
    private lateinit var api: WebTestClient

    @BeforeEach
    fun auth() {
        token = freshToken()
        api = authedClient(token)
    }

    // ─── List ─────────────────────────────────────────────────────────────────

    @Test
    fun `GET brand - returns 200 with slice`() {
        api.get().uri("/api/v1/brand")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items").isArray
            .jsonPath("$.nextToken").exists()
    }

    @Test
    fun `GET brand - respects limit param`() {
        repeat(3) { i ->
            api.post().uri("/api/v1/brand")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapOf("name" to "LimitBrand-${UUID.randomUUID()}"))
                .exchange()
                .expectStatus().isOk
        }

        api.get().uri("/api/v1/brand?limit=1")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.nextToken").isNotEmpty
    }

    // ─── Create ───────────────────────────────────────────────────────────────

    @Test
    fun `POST brand - creates brand successfully`() {
        val name = "Brand-${UUID.randomUUID()}"

        api.post().uri("/api/v1/brand")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to name, "description" to "Test brand"))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo(name)
            .jsonPath("$.description").isEqualTo("Test brand")
            .jsonPath("$.id").isNotEmpty
    }

    @Test
    fun `POST brand - creates brand without description`() {
        val name = "Brand-${UUID.randomUUID()}"

        api.post().uri("/api/v1/brand")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to name))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo(name)
            .jsonPath("$.description").doesNotExist()
    }

    @Test
    fun `POST brand - returns 400 when name is blank`() {
        api.post().uri("/api/v1/brand")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to ""))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `POST brand - returns 400 on duplicate name`() {
        val name = "DupBrand-${UUID.randomUUID()}"

        api.post().uri("/api/v1/brand")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to name))
            .exchange()
            .expectStatus().isOk

        api.post().uri("/api/v1/brand")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to name))
            .exchange()
            .expectStatus().isBadRequest
    }

    // ─── Find by ID ───────────────────────────────────────────────────────────

    @Test
    fun `GET brand-id - returns brand when found`() {
        val name = "FindBrand-${UUID.randomUUID()}"
        val id = api.post().uri("/api/v1/brand")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to name))
            .exchange()
            .expectBody(Map::class.java)
            .returnResult().responseBody!!["id"] as String

        api.get().uri("/api/v1/brand/id/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(id)
            .jsonPath("$.name").isEqualTo(name)
    }

    @Test
    fun `GET brand-id - returns 404 when not found`() {
        api.get().uri("/api/v1/brand/id/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isNotFound
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Test
    fun `PUT brand - updates name and description`() {
        val id = createBrand("UpdateMe-${UUID.randomUUID()}")
        val newName = "Updated-${UUID.randomUUID()}"

        api.put().uri("/api/v1/brand/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to newName, "description" to "new desc"))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo(newName)
            .jsonPath("$.description").isEqualTo("new desc")
    }

    @Test
    fun `PUT brand - clears description when null`() {
        val id = createBrand("ClearDesc-${UUID.randomUUID()}", "some desc")

        api.put().uri("/api/v1/brand/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to "ClearDesc-Updated-${UUID.randomUUID()}"))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.description").doesNotExist()
    }

    @Test
    fun `PUT brand - returns 400 when name is blank`() {
        val id = createBrand("BlankUpdate-${UUID.randomUUID()}")

        api.put().uri("/api/v1/brand/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to ""))
            .exchange()
            .expectStatus().isBadRequest
    }

    // ─── Tag operations ───────────────────────────────────────────────────────

    @Test
    fun `PATCH brand add-tag and remove-tag`() {
        val brandId = createBrand("TagBrand-${UUID.randomUUID()}")
        val tagId = createTag("TagForBrand-${UUID.randomUUID()}")

        // Add tag
        api.patch().uri("/api/v1/brand/add-tag")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("brandId" to brandId, "tagId" to tagId))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.tags[0].id").isEqualTo(tagId)

        // Add same tag again — should return 400
        api.patch().uri("/api/v1/brand/add-tag")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("brandId" to brandId, "tagId" to tagId))
            .exchange()
            .expectStatus().isBadRequest

        // Remove tag
        api.patch().uri("/api/v1/brand/remove-tag")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("brandId" to brandId, "tagId" to tagId))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.tags").isEmpty
    }

    @Test
    fun `PATCH brand add-tag - returns 400 for unknown brand`() {
        val tagId = createTag("OrphanTag-${UUID.randomUUID()}")

        api.patch().uri("/api/v1/brand/add-tag")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("brandId" to UUID.randomUUID().toString(), "tagId" to tagId))
            .exchange()
            .expectStatus().isBadRequest
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun createBrand(name: String, description: String? = null): String {
        val body = buildMap<String, Any> {
            put("name", name)
            if (description != null) put("description", description)
        }
        return api.post().uri("/api/v1/brand")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus().isOk
            .expectBody(Map::class.java)
            .returnResult().responseBody!!["id"] as String
    }

    private fun createTag(name: String): String =
        api.post().uri("/api/v1/tag")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to name))
            .exchange()
            .expectStatus().isOk
            .expectBody(Map::class.java)
            .returnResult().responseBody!!["id"] as String
}
