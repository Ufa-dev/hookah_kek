package com.hookah.kek_hookah.ui.context

import com.hookah.kek_hookah.feature.auth.api.dto.AuthResponse
import com.hookah.kek_hookah.feature.user.model.UserId
import com.vaadin.flow.server.VaadinSession
import java.time.Instant
import java.time.OffsetDateTime

class AuthContext {
    var accessToken: String? = null
    var refreshToken: String? = null
    var accessTokenExpiresAt: OffsetDateTime? = null
    var refreshTokenExpiresAt: OffsetDateTime? = null
    var userId: UserId? = null

    fun isAuthenticated(): Boolean = !accessToken.isNullOrBlank()

    fun updateFromAuthResponse(response: AuthResponse) {
        accessToken = response.accessToken
        refreshToken = response.refreshToken
        accessTokenExpiresAt = OffsetDateTime.now().plusSeconds(response.expiresIn)
        refreshTokenExpiresAt = OffsetDateTime.now().plusSeconds(response.refreshExpiresIn)
        userId = response.userId
    }

    private fun clear() {
        accessToken = null
        refreshToken = null
        accessTokenExpiresAt = null
        refreshTokenExpiresAt = null
    }

    companion object {
        fun get(): AuthContext {
            val session = VaadinSession.getCurrent()
            var context = session.getAttribute(AuthContext::class.java)
            if (context == null) {
                context = AuthContext()
                session.setAttribute(AuthContext::class.java, context)
            }
            return context
        }
    }
}