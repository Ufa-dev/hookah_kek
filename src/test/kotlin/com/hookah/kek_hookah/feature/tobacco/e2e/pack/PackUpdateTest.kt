package com.hookah.kek_hookah.feature.tobacco.e2e.pack

import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

@IntegrationTest
class PackUpdateTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should update pack`() = runTest {
        val client = unauthorizedClient.randomUser()
        val pack = client.createPackAndGet(totalWeightGrams = 100, currentWeightGrams = 50)
        val newName = "upd-${UUID.randomUUID().toString().take(8)}"

        val updated = client.updatePack(
            id = pack.id.id,
            name = newName,
            totalWeightGrams = 200,
            currentWeightGrams = 80
        ).expectStatus().isOk
            .expectBody<FlavorPack>()
            .returnResult().responseBody!!

        assertAll(
            { assertEquals(pack.id, updated.id) },
            { assertEquals(newName, updated.name) },
            { assertEquals(200, updated.totalWeightGrams) },
            { assertEquals(80, updated.currentWeightGrams) },
            { assertTrue(updated.updatedAt >= pack.updatedAt) }
        )
    }

    @Test
    fun `should return 401 when updating pack without authentication`() = runTest {
        unauthorizedClient.put()
            .uri("$PACK_URL/${UUID.randomUUID()}")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to "upd", "totalWeightGrams" to 100, "currentWeightGrams" to 50))
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 400 when pack name is blank on update`() = runTest {
        val client = unauthorizedClient.randomUser()
        val pack = client.createPackAndGet()

        client.updatePack(id = pack.id.id, name = "  ")
            .expectStatus().isBadRequest
    }
}
