package com.hookah.kek_hookah.api

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

class PackApiIT : BaseApiIT() {

    private lateinit var token: String
    private lateinit var api: WebTestClient

    @BeforeEach
    fun auth() {
        token = freshToken()
        api = authedClient(token)
    }

    // ─── List ─────────────────────────────────────────────────────────────────

    @Test
    fun `GET pack - returns 200 with slice`() {
        api.get().uri("/api/v1/pack")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items").isArray
            .jsonPath("$.nextToken").exists()
    }

    @Test
    fun `GET pack - cursor pagination works`() {
        val id1 = "aaaa-${UUID.randomUUID()}"
        val id2 = "aaab-${UUID.randomUUID()}"
        createPack(id1)
        createPack(id2)

        val firstPage = api.get().uri("/api/v1/pack?limit=1&after=aaaa")
            .exchange()
            .expectStatus().isOk
            .expectBody(Map::class.java)
            .returnResult().responseBody!!

        @Suppress("UNCHECKED_CAST")
        val items = firstPage["items"] as List<Map<String, Any>>
        assert(items.isNotEmpty()) { "Expected at least one pack after cursor 'aaaa'" }
    }

    // ─── Create ───────────────────────────────────────────────────────────────

    @Test
    fun `POST pack - creates pack successfully`() {
        val id = "pack-${UUID.randomUUID()}"

        api.post().uri("/api/v1/pack")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf(
                "id" to id,
                "name" to "Test Pack",
                "currentWeightGrams" to 50,
                "totalWeightGrams" to 100,
            ))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(id)
            .jsonPath("$.name").isEqualTo("Test Pack")
            .jsonPath("$.currentWeightGrams").isEqualTo(50)
            .jsonPath("$.totalWeightGrams").isEqualTo(100)
            .jsonPath("$.flavorId").doesNotExist()
    }

    @Test
    fun `POST pack - returns 400 when id is blank`() {
        api.post().uri("/api/v1/pack")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("id" to "", "name" to "X", "currentWeightGrams" to 0, "totalWeightGrams" to 100))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `POST pack - returns 400 when name is blank`() {
        api.post().uri("/api/v1/pack")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("id" to "x-${UUID.randomUUID()}", "name" to "", "currentWeightGrams" to 0, "totalWeightGrams" to 100))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `POST pack - returns 400 when duplicate id`() {
        val id = "dup-${UUID.randomUUID()}"
        createPack(id)

        api.post().uri("/api/v1/pack")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("id" to id, "name" to "Dup", "currentWeightGrams" to 0, "totalWeightGrams" to 100))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `POST pack - returns 400 when currentWeight exceeds totalWeight`() {
        api.post().uri("/api/v1/pack")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf(
                "id" to "bad-${UUID.randomUUID()}",
                "name" to "Bad",
                "currentWeightGrams" to 200,
                "totalWeightGrams" to 100,
            ))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `POST pack - returns 400 when totalWeight is zero`() {
        api.post().uri("/api/v1/pack")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf(
                "id" to "zero-${UUID.randomUUID()}",
                "name" to "Zero",
                "currentWeightGrams" to 0,
                "totalWeightGrams" to 0,
            ))
            .exchange()
            .expectStatus().isBadRequest
    }

    // ─── Find by ID ───────────────────────────────────────────────────────────

    @Test
    fun `GET pack-id - returns pack when found`() {
        val id = "find-${UUID.randomUUID()}"
        createPack(id)

        api.get().uri("/api/v1/pack/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(id)
            .jsonPath("$.name").isEqualTo("Pack $id")
    }

    @Test
    fun `GET pack-id - returns 404 when not found`() {
        api.get().uri("/api/v1/pack/nonexistent-pack-xyz-123")
            .exchange()
            .expectStatus().isNotFound
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Test
    fun `PUT pack - updates weight and name`() {
        val id = "upd-${UUID.randomUUID()}"
        createPack(id)

        api.put().uri("/api/v1/pack/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf(
                "name" to "Updated Name",
                "currentWeightGrams" to 25,
                "totalWeightGrams" to 100,
            ))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("Updated Name")
            .jsonPath("$.currentWeightGrams").isEqualTo(25)
    }

    @Test
    fun `PUT pack - returns 400 when pack not found`() {
        api.put().uri("/api/v1/pack/no-such-pack")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to "X", "currentWeightGrams" to 0, "totalWeightGrams" to 100))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `PUT pack - returns 400 when name is blank`() {
        val id = "blankname-${UUID.randomUUID()}"
        createPack(id)

        api.put().uri("/api/v1/pack/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to "", "currentWeightGrams" to 0, "totalWeightGrams" to 100))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `PUT pack - returns 400 when currentWeight exceeds totalWeight`() {
        val id = "badupd-${UUID.randomUUID()}"
        createPack(id)

        api.put().uri("/api/v1/pack/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to "X", "currentWeightGrams" to 300, "totalWeightGrams" to 100))
            .exchange()
            .expectStatus().isBadRequest
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Test
    fun `DELETE pack - deletes and returns 204`() {
        val id = "del-${UUID.randomUUID()}"
        createPack(id)

        api.delete().uri("/api/v1/pack/$id")
            .exchange()
            .expectStatus().isNoContent

        api.get().uri("/api/v1/pack/$id")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `DELETE pack - returns 400 when not found`() {
        api.delete().uri("/api/v1/pack/no-such-pack-delete")
            .exchange()
            .expectStatus().isBadRequest
    }

    // ─── Auth ─────────────────────────────────────────────────────────────────

    @Test
    fun `POST pack - returns 401 without token`() {
        client.post().uri("/api/v1/pack")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("id" to "x", "name" to "x", "currentWeightGrams" to 0, "totalWeightGrams" to 100))
            .exchange()
            .expectStatus().isUnauthorized
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private fun createPack(id: String) {
        api.post().uri("/api/v1/pack")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf(
                "id" to id,
                "name" to "Pack $id",
                "currentWeightGrams" to 50,
                "totalWeightGrams" to 100,
            ))
            .exchange()
            .expectStatus().isOk
    }
}
