package com.hookah.kek_hookah.feature.tobacco.e2e.pack

import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

@IntegrationTest
class PackDeleteTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should delete pack successfully`() = runTest {
        val client = unauthorizedClient.randomUser()
        val pack = client.createPackAndGet()

        client.deletePack(pack.id.id)
            .expectStatus().isNoContent

        client.getPackById(pack.id.id)
            .expectStatus().isNotFound
    }

    @Test
    fun `should return 401 when deleting pack without authentication`() = runTest {
        unauthorizedClient.delete()
            .uri("$PACK_URL/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
