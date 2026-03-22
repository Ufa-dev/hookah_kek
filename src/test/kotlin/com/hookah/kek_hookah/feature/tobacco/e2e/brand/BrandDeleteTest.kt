package com.hookah.kek_hookah.feature.tobacco.e2e.brand

import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

@IntegrationTest
class BrandDeleteTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should delete brand successfully`() = runTest {
        val client = unauthorizedClient.randomUser()
        val brand = client.createBrandAndGet()

        client.deleteBrand(brand.id.id)
            .expectStatus().isNoContent

        client.getBrandById(brand.id.id)
            .expectStatus().isNotFound
    }

    @Test
    fun `should return 401 when deleting brand without authentication`() = runTest {
        unauthorizedClient.delete()
            .uri("$BRAND_URL/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
