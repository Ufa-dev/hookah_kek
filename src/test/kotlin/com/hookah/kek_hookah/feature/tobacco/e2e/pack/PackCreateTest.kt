package com.hookah.kek_hookah.feature.tobacco.e2e.pack

import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

@IntegrationTest
class PackCreateTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should create pack successfully`() = runTest {
        val client = unauthorizedClient.randomUser()
        val name = "pack-${UUID.randomUUID().toString().take(8)}"

        val pack = client.createPackAndGet(name = name, totalWeightGrams = 200, currentWeightGrams = 100)

        assertAll(
            { assertNotNull(pack.id) },
            { assertEquals(name, pack.name) },
            { assertEquals(200, pack.totalWeightGrams) },
            { assertEquals(100, pack.currentWeightGrams) },
            { assertNull(pack.flavorId) },
            { assertNotNull(pack.createdAt) },
            { assertNotNull(pack.updatedBy) }
        )
    }

    @Test
    fun `should return 401 when creating pack without authentication`() = runTest {
        unauthorizedClient.post()
            .uri(PACK_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("tagId" to "some-tag", "name" to "pack", "totalWeightGrams" to 100, "currentWeightGrams" to 50))
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 400 when pack name is blank`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.createPack(name = "  ")
            .expectStatus().isBadRequest
    }

    @Test
    fun `should return 400 when totalWeightGrams is zero`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.createPack(totalWeightGrams = 0)
            .expectStatus().isBadRequest
    }
}
