package com.hookah.kek_hookah.feature.tobacco.e2e.user

import com.hookah.kek_hookah.feature.tobacco.e2e.auth.randomUser
import com.hookah.kek_hookah.feature.tobacco.support.IntegrationTest
import com.hookah.kek_hookah.feature.user.api.dto.UserForUpdateDto
import com.hookah.kek_hookah.feature.user.model.User
import com.maverick.landcruiser.server.e2e.user.getMe
import com.maverick.landcruiser.server.e2e.user.patchMe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

@IntegrationTest
class UserUpdateTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    // ========== Happy Path Tests ==========

    @Test
    fun `should update only user name`() = runTest {
        val client = unauthorizedClient.randomUser()
        val original = client.getMe()
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        val updateRequest = UserForUpdateDto(name = "Updated Name", email = null)

        val result = client.patchMe(updateRequest)
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        assertAll(
            { assertEquals(original.id, result.id) },
            { assertEquals("Updated Name", result.name) },
            { assertEquals(original.email, result.email) },
            { assertTrue(result.updatedAt.isAfter(original.updatedAt)) }
        )
    }

    @Test
    fun `should update only user email`() = runTest {
        val client = unauthorizedClient.randomUser()
        val original = client.getMe()
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        val newEmail = "new-${UUID.randomUUID()}@test.com"
        val updateRequest = UserForUpdateDto(name = null, email = newEmail)

        val result = client.patchMe(updateRequest)
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        assertAll(
            { assertEquals(original.id, result.id) },
            { assertEquals(original.name, result.name) },
            { assertEquals(newEmail, result.email) },
            { assertTrue(result.updatedAt.isAfter(original.updatedAt)) }
        )
    }

    @Test
    fun `should update both name and email`() = runTest {
        val client = unauthorizedClient.randomUser()
        val original = client.getMe()
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        val newEmail = "updated-${UUID.randomUUID()}@test.com"
        val updateRequest = UserForUpdateDto(
            name = "New Name",
            email = newEmail
        )

        val result = client.patchMe(updateRequest)
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        assertAll(
            { assertEquals(original.id, result.id) },
            { assertEquals("New Name", result.name) },
            { assertEquals(newEmail, result.email) },
            { assertTrue(result.updatedAt.isAfter(original.updatedAt)) }
        )
    }

    // ========== Validation Tests ==========

    @Test
    fun `should reject update with blank name`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.getMe()
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        val updateRequest = UserForUpdateDto(name = "", email = null)

        client.patchMe(updateRequest)
            .expectStatus().isBadRequest
    }

    @Test
    fun `should reject update with whitespace-only name`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.getMe()
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        val updateRequest = UserForUpdateDto(name = "   ", email = null)

        client.patchMe(updateRequest)
            .expectStatus().isBadRequest
    }

    @Test
    fun `should reject update with blank email`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.getMe()
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        val updateRequest = UserForUpdateDto(name = null, email = "")

        client.patchMe(updateRequest)
            .expectStatus().isBadRequest
    }

    @Test
    fun `should reject update with whitespace-only email`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.getMe()
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        val updateRequest = UserForUpdateDto(name = null, email = "   ")

        client.patchMe(updateRequest)
            .expectStatus().isBadRequest
    }

    // ========== Edge Cases Tests ==========

    @Test
    fun `should reject update with duplicate email`() = runTest {
        val client1 = unauthorizedClient.randomUser()
        val client2 = unauthorizedClient.randomUser()

        val user1 = client1.getMe()
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        client2.getMe()
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        val updateRequest = UserForUpdateDto(name = null, email = user1.email)

        client2.patchMe(updateRequest)
            .expectStatus().is5xxServerError
    }

    @Test
    fun `should accept name at max length (255 chars)`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.getMe()
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        val longName = "A".repeat(255)
        val updateRequest = UserForUpdateDto(name = longName, email = null)

        val result = client.patchMe(updateRequest)
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        assertEquals(longName, result.name)
    }

    @Test
    fun `should accept email at max length (255 chars)`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.getMe()
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        val localPart = "a".repeat(255 - "@test.com".length)
        val longEmail = "$localPart@test.com"
        val updateRequest = UserForUpdateDto(name = null, email = longEmail)

        val result = client.patchMe(updateRequest)
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        assertEquals(longEmail, result.email)
    }

    @Test
    fun `should accept name with unicode characters`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.getMe()
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        val unicodeName = "Имя 名前 🧊"
        val updateRequest = UserForUpdateDto(name = unicodeName, email = null)

        val result = client.patchMe(updateRequest)
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        assertEquals(unicodeName, result.name)
    }

    @Test
    fun `should accept name with special characters`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.getMe()
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        val specialName = "John O'Brien-Smith Jr."
        val updateRequest = UserForUpdateDto(name = specialName, email = null)

        val result = client.patchMe(updateRequest)
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        assertEquals(specialName, result.name)
    }

    @Test
    fun `should accept invalid email format (no validation in UserForUpdate)`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.getMe()
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        val invalidEmail = "not-an-email-${UUID.randomUUID()}"
        val updateRequest = UserForUpdateDto(name = null, email = invalidEmail)

        val result = client.patchMe(updateRequest)
            .expectStatus().isOk
            .expectBody<User>()
            .returnResult()
            .responseBody!!

        assertEquals(invalidEmail, result.email)
    }
}
