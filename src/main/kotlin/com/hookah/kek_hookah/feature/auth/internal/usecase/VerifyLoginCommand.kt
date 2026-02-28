package com.hookah.kek_hookah.feature.auth.internal.usecase

import com.hookah.kek_hookah.feature.auth.internal.repository.PasswordRepository
import com.hookah.kek_hookah.feature.user.UserService
import com.hookah.kek_hookah.feature.user.model.User
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component


private const val INVALID_EMAIL_OR_PASSWORD = "Invalid email or password"

@Component
class VerifyLoginCommand(
    private val repository: PasswordRepository,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder

) {
    suspend fun execute(email: String, password: String): User {
        val user = userService.findByEmail(email)
            ?: throw BadCredentialsException(INVALID_EMAIL_OR_PASSWORD)

        val passwordRecord = repository.findByUserId(user.id)
            ?: throw BadCredentialsException(INVALID_EMAIL_OR_PASSWORD)

        if (!passwordEncoder.matches(password, passwordRecord.password)) {
            throw BadCredentialsException(INVALID_EMAIL_OR_PASSWORD)
        }

        return user
    }

}
