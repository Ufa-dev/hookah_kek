package com.hookah.kek_hookah.feature.tobacco.support

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.context.ImportTestcontainers
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.web.server.reactive.context.ReactiveWebServerApplicationContext
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Lazy
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container

/**
 * Shared Testcontainers + WebTestClient configuration for [IntegrationTest]-annotated test classes.
 */
@TestConfiguration
@ImportTestcontainers(Containers::class)
class IntegrationTestConfig {

    @Bean
    @Lazy
    fun webTestClient(applicationContext: ApplicationContext): WebTestClient {
        val ctx = applicationContext as ReactiveWebServerApplicationContext
        val port = ctx.webServer!!.port
        return WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port")
            .build()
    }
}

object Containers {
    @Container
    @ServiceConnection
    @JvmField
    val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
        .withDatabaseName("hookah_test")
        .withUsername("test")
        .withPassword("test")
}
