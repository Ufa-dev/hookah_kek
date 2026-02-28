package com.hookah.kek_hookah.feature.auth.internal.usecase

import com.hookah.kek_hookah.feature.auth.internal.repository.PasswordRepository
import com.hookah.kek_hookah.feature.auth.model.Password
import com.hookah.kek_hookah.feature.user.model.UserId
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class CreatePasswordCommand(
    private val repository: PasswordRepository,
    private val passwordEncoder: PasswordEncoder

) {

    suspend fun execute(userId: UserId, password: String) {
        val hashedPassword = passwordEncoder.encode(password)
            ?: throw IllegalArgumentException("Non valid password")

        val password = Password(
            userId = userId,
            password = hashedPassword,
            generatedAt = OffsetDateTime.now(),
        )

        repository.insert(password)
    }

}
