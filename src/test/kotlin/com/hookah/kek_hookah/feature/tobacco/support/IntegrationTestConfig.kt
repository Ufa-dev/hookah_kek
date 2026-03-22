package com.hookah.kek_hookah.feature.tobacco.support

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.PostgreSQLContainer

/**
 * Shared test configuration for all integration tests.
 * Provides a singleton PostgreSQL container and registers test properties.
 */
class IntegrationTestConfig : ApplicationContextInitializer<ConfigurableApplicationContext> {

    companion object {
        /**
         * Singleton PostgreSQL container shared across all tests.
         * Using singleton pattern to avoid starting a new container for each test class.
         */
        val postgresContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .apply {
                start()
            }
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        // Register database properties
        TestPropertyValues.of(
            "spring.r2dbc.url=r2dbc:postgresql://${postgresContainer.host}:${postgresContainer.firstMappedPort}/${postgresContainer.databaseName}",
            "spring.r2dbc.username=${postgresContainer.username}",
            "spring.r2dbc.password=${postgresContainer.password}",
            "spring.flyway.url=${postgresContainer.jdbcUrl}",
            "spring.flyway.user=${postgresContainer.username}",
            "spring.flyway.password=${postgresContainer.password}",
            // JWT configuration
            "app.security.jwt.secret=test-secret-key-for-jwt-token-generation-minimum-256-bits",
            "app.security.jwt.access-token-expiration-ms=1800000",
            "app.security.jwt.refresh-token-expiration-ms=86400000"
        ).applyTo(applicationContext.environment)
    }
}
