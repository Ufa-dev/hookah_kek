package com.hookah.kek_hookah.feature.user.internal.usecase

import com.hookah.kek_hookah.feature.user.internal.repository.UserRepository
import com.hookah.kek_hookah.feature.user.model.User
import com.hookah.kek_hookah.feature.user.model.UserForUpdate
import com.hookah.kek_hookah.feature.user.model.UserUpdatedEvent
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime


@Component
class UpdateUserCommand(
    private val repository: UserRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
) {

    suspend fun execute(request: UserForUpdate): User {
        val existing = repository.findById(request.userId)
            ?: throw IllegalArgumentException("User not found!")

        val email = request.email ?: existing.email

        val updated = existing.copy(
            name = request.name ?: existing.name,
            email = email.lowercase(),
            updatedAt = OffsetDateTime.now(),
        )

        return tx.executeAndAwait {
            repository.update(updated)
        }.also { user ->
            eventPublisher + UserUpdatedEvent(
                before = existing,
                after = user,
                publishedAt = OffsetDateTime.now()
            )
        }
    }

}
