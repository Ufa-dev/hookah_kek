package com.hookah.kek_hookah.feature.tobacco.e2e.flavor

import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

@IntegrationTest
class FlavorDeleteTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should delete flavor successfully`() = runTest {
        val client = unauthorizedClient.randomUser()
        val flavor = client.createFlavorAndGet()

        client.deleteFlavor(flavor.id.id)
            .expectStatus().isNoContent

        client.getFlavorById(flavor.id.id)
            .expectStatus().isNotFound
    }

    @Test
    fun `should return 401 when deleting flavor without authentication`() = runTest {
        unauthorizedClient.delete()
            .uri("$FLAVOR_URL/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
