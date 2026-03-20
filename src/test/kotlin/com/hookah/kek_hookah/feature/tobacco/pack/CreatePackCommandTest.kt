package com.hookah.kek_hookah.feature.tobacco.pack

import com.hookah.kek_hookah.feature.tobacco.pack.internal.repository.PackRepository
import com.hookah.kek_hookah.feature.tobacco.pack.internal.usecase.CreatePackCommand
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackForCreate
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackId
import com.hookah.kek_hookah.feature.user.model.UserId
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.transaction.reactive.TransactionalOperator
import java.time.OffsetDateTime
import java.util.*

/**
 * Unit tests for CreatePackCommand.
 * Validation cases throw before tx.executeAndAwait is reached, so no TX stub is needed.
 * The happy-path (successful insert) is covered by PackApiIT integration tests.
 */
class CreatePackCommandTest {

    private val repository = mockk<PackRepository>()
    private val eventPublisher = mockk<EventPublisher>()
    private val tx = mockk<TransactionalOperator>()
    private val command = CreatePackCommand(repository, eventPublisher, tx)

    private val userId = UserId(UUID.randomUUID())

    private fun existingPack(id: String) = FlavorPack(
        id = PackId(id),
        name = "Existing",
        flavorId = null,
        currentWeightGrams = 0,
        totalWeightGrams = 100,
        createdAt = OffsetDateTime.now(),
        updatedAt = OffsetDateTime.now(),
        updatedBy = userId,
    )

    @Test
    fun `throws when pack id already exists`() = runTest {
        coEvery { repository.findById(PackId("dup")) } returns existingPack("dup")

        assertThrows<IllegalArgumentException> {
            command.execute(PackForCreate("dup", "Dup Pack", null, 0, 100, userId))
        }
    }

    @Test
    fun `throws when currentWeight exceeds totalWeight`() = runTest {
        coEvery { repository.findById(PackId("bad")) } returns null

        assertThrows<IllegalArgumentException> {
            command.execute(PackForCreate("bad", "Bad Pack", null, 200, 100, userId))
        }
    }

    @Test
    fun `throws when totalWeight is zero`() = runTest {
        coEvery { repository.findById(PackId("zero")) } returns null

        assertThrows<IllegalArgumentException> {
            command.execute(PackForCreate("zero", "Zero Pack", null, 0, 0, userId))
        }
    }

    @Test
    fun `throws when currentWeight is negative`() = runTest {
        coEvery { repository.findById(PackId("neg")) } returns null

        assertThrows<IllegalArgumentException> {
            command.execute(PackForCreate("neg", "Neg Pack", null, -1, 100, userId))
        }
    }
}
