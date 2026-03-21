package com.hookah.kek_hookah.feature.tobacco.pack

import com.hookah.kek_hookah.feature.tobacco.pack.internal.repository.PackRepository
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackId
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackTagId
import com.hookah.kek_hookah.feature.user.model.UserId
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.OffsetDateTime
import java.util.*

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class PackRepositoryIT {

    companion object {
        @Container
        @ServiceConnection
        @JvmField
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("hookah_test")
            .withUsername("test")
            .withPassword("test")
    }

    @Autowired
    lateinit var repository: PackRepository

    private val userId = UserId(UUID.randomUUID())

    private fun pack(id: String, current: Int = 50, total: Int = 100) = FlavorPack(
        id = PackId(id),
        tagId = PackTagId(id),
        name = id,
        flavorId = null,
        currentWeightGrams = current,
        totalWeightGrams = total,
        createdAt = OffsetDateTime.now(),
        updatedAt = OffsetDateTime.now(),
        updatedBy = userId,
    )

    @Test
    fun `insert and findById`() = runTest {
        val inserted = repository.insert(pack("it-pack-1"))
        val found = repository.findById(PackId("it-pack-1"))

        assertNotNull(found)
        assertEquals("it-pack-1", found!!.id.id)
        assertEquals(50, found.currentWeightGrams)
        assertEquals(100, found.totalWeightGrams)
    }

    @Test
    fun `findById returns null for missing id`() = runTest {
        val result = repository.findById(PackId("non-existent-pack"))
        assertNull(result)
    }

    @Test
    fun `update persists changes`() = runTest {
        repository.insert(pack("it-pack-update"))

        val existing = repository.findById(PackId("it-pack-update"))!!
        val updated = existing.copy(currentWeightGrams = 25, updatedAt = OffsetDateTime.now())
        repository.update(updated)

        val found = repository.findById(PackId("it-pack-update"))
        assertEquals(25, found!!.currentWeightGrams)
    }

    @Test
    fun `delete removes the pack`() = runTest {
        repository.insert(pack("it-pack-delete"))
        repository.delete(PackId("it-pack-delete"))

        val found = repository.findById(PackId("it-pack-delete"))
        assertNull(found)
    }

    @Test
    fun `findAll cursor pagination`() = runTest {
        // Insert in known order (ids sorted lexicographically)
        repository.insert(pack("zz-page-a"))
        repository.insert(pack("zz-page-b"))
        repository.insert(pack("zz-page-c"))

        val firstPage = repository.findAll(2, null)  // first 2 items
        assertEquals(2, firstPage.size)

        val secondPage = repository.findAll(2, firstPage.last().id.id)
        assertEquals(1, secondPage.size)
        assertEquals("zz-page-c", secondPage.first().id.id)
    }
}
