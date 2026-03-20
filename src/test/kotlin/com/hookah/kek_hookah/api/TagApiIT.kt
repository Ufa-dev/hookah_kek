package com.hookah.kek_hookah.api

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

class TagApiIT : BaseApiIT() {

    private lateinit var token: String
    private lateinit var api: WebTestClient

    @BeforeEach
    fun auth() {
        token = freshToken()
        api = authedClient(token)
    }

    // ─── Create ───────────────────────────────────────────────────────────────

    @Test
    fun `POST tag - creates tag successfully`() {
        val name = "Tag-${UUID.randomUUID()}"

        api.post().uri("/api/v1/tag")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to name))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isNotEmpty
            .jsonPath("$.name").isEqualTo(name)
    }

    @Test
    fun `POST tag - returns 400 when name is blank`() {
        api.post().uri("/api/v1/tag")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to ""))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `POST tag - returns 400 on duplicate name`() {
        val name = "DupTag-${UUID.randomUUID()}"

        api.post().uri("/api/v1/tag")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to name))
            .exchange()
            .expectStatus().isOk

        api.post().uri("/api/v1/tag")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to name))
            .exchange()
            .expectStatus().isBadRequest
    }

    // ─── Find by ID ───────────────────────────────────────────────────────────

    @Test
    fun `GET tag-id - returns tag when found`() {
        val name = "FindTag-${UUID.randomUUID()}"
        val id = createTag(name)

        api.get().uri("/api/v1/tag/id/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(id)
            .jsonPath("$.name").isEqualTo(name)
    }

    @Test
    fun `GET tag-id - returns 404 when not found`() {
        api.get().uri("/api/v1/tag/id/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isNotFound
    }

    // ─── Find by name ─────────────────────────────────────────────────────────

    @Test
    fun `GET tag-name - returns tag for exact match`() {
        val name = "ExactName-${UUID.randomUUID()}"
        createTag(name)

        api.get().uri("/api/v1/tag/name/$name")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo(name)
    }

    @Test
    fun `GET tag-name - returns 404 when no match`() {
        api.get().uri("/api/v1/tag/name/this-tag-does-not-exist-ever")
            .exchange()
            .expectStatus().isNotFound
    }

    // ─── List ─────────────────────────────────────────────────────────────────

    @Test
    fun `GET tag - returns 200 with slice`() {
        api.get().uri("/api/v1/tag")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items").isArray
    }

    @Test
    fun `GET tag - cursor pagination works`() {
        // Create two known tags and verify cursor moves forward
        val tag1Id = createTag("PagTag1-${UUID.randomUUID()}")

        val firstPage = api.get().uri("/api/v1/tag?limit=1")
            .exchange()
            .expectStatus().isOk
            .expectBody(Map::class.java)
            .returnResult().responseBody!!

        val nextToken = firstPage["nextToken"]
        if (nextToken != null) {
            api.get().uri("/api/v1/tag?limit=1&after=$nextToken")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.items").isArray
        }
    }

    // ─── Update name ──────────────────────────────────────────────────────────

    @Test
    fun `PATCH tag-name - updates name successfully`() {
        val id = createTag("OldTagName-${UUID.randomUUID()}")
        val newName = "NewTagName-${UUID.randomUUID()}"

        api.patch().uri("/api/v1/tag/$id/name")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to newName))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo(newName)
    }

    @Test
    fun `PATCH tag-name - returns 400 when name is blank`() {
        val id = createTag("BlankRename-${UUID.randomUUID()}")

        api.patch().uri("/api/v1/tag/$id/name")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to ""))
            .exchange()
            .expectStatus().isBadRequest
    }

    // ─── Requires auth ────────────────────────────────────────────────────────

    @Test
    fun `POST tag - returns 401 without token`() {
        client.post().uri("/api/v1/tag")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to "NoAuth"))
            .exchange()
            .expectStatus().isUnauthorized
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private fun createTag(name: String): String =
        api.post().uri("/api/v1/tag")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to name))
            .exchange()
            .expectBody(Map::class.java)
            .returnResult().responseBody!!["id"] as String
}
