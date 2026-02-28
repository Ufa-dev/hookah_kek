package com.hookah.kek_hookah.infrastructure.config.security

import com.hookah.kek_hookah.feature.auth.model.UserPrincipal
import com.hookah.kek_hookah.feature.user.model.UserId
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationConverter : Converter<Jwt, Mono<out AbstractAuthenticationToken>> {

    override fun convert(jwt: Jwt): Mono<out AbstractAuthenticationToken> {
        val userId = jwt.subject
        val principal = UserPrincipal(id = UserId(id = userId))

        return UsernamePasswordAuthenticationToken(
            /* principal = */ principal,
            /* credentials = */ null,
            /* authorities = */ emptyList()
        ).let {
            Mono.just(it)
        }
    }
}

