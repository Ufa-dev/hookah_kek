package com.hookah.kek_hookah.feature.user.internal.usecase

import com.hookah.kek_hookah.feature.user.internal.repository.UserRepository
import com.hookah.kek_hookah.feature.user.model.User
import com.hookah.kek_hookah.feature.user.model.UserCreatedEvent
import com.hookah.kek_hookah.feature.user.model.UserForCreate
import com.hookah.kek_hookah.feature.user.model.UserId
import com.hookah.kek_hookah.infrastructure.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.OffsetDateTime

@Component
class CreateUserCommand(
    private val repository: UserRepository,
    private val eventPublisher: EventPublisher,
    private val tx: TransactionalOperator,
) {

    suspend fun execute(request: UserForCreate): User {
        repository.findByEmail(request.email)
            ?.let { throw IllegalArgumentException("User with this email already exist!") }

        return User(
            id = UserId(),
            name = request.name,
            email = request.email.lowercase(),
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
        ).let { user ->
            tx.executeAndAwait { repository.insert(user) }
        }.also { user ->
            eventPublisher + UserCreatedEvent(
                user = user,
                publishedAt = OffsetDateTime.now()
            )
        }
    }

}
