package com.hookah.kek_hookah.feature.tobacco.support

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

/**
 * Meta-annotation for integration tests.
 *
 * Automatically configures:
 * - Spring Boot test context with random port
 * - WebTestClient for HTTP testing
 * - PostgreSQL Testcontainer (shared singleton)
 * - Database properties (R2DBC + Flyway)
 * - JWT test configuration
 *
 * Usage:
 * ```kotlin
 * @IntegrationTest
 * class MyTest {
 *     @Autowired
 *     private lateinit var webTestClient: WebTestClient
 *
 *     @Test
 *     fun myTest() = runTest {
 *         // test code
 *     }
 * }
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ContextConfiguration(initializers = [IntegrationTestConfig::class])
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
annotation class IntegrationTest
