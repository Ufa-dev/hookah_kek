package com.hookah.kek_hookah.feature.user

import com.hookah.kek_hookah.feature.user.internal.repository.UserRepository
import com.hookah.kek_hookah.feature.user.internal.usecase.CreateUserCommand
import com.hookah.kek_hookah.feature.user.internal.usecase.UpdateUserCommand
import com.hookah.kek_hookah.feature.user.model.User
import com.hookah.kek_hookah.feature.user.model.UserForCreate
import com.hookah.kek_hookah.feature.user.model.UserForUpdate
import com.hookah.kek_hookah.feature.user.model.UserId
import org.springframework.stereotype.Component

@Component
class UserService(
    private val repository: UserRepository,
    private val createUserCommand: CreateUserCommand,
    private val updateUserCommand: UpdateUserCommand
) {

    suspend fun findById(id: UserId): User? {
        return repository.findById(id)
    }

    suspend fun findByEmail(email: String): User? {
        return repository.findByEmail(email.lowercase())
    }

    suspend fun create(request: UserForCreate): User {
        return createUserCommand.execute(request)
    }

    suspend fun update(request: UserForUpdate): User {
        return updateUserCommand.execute(request)
    }
}
