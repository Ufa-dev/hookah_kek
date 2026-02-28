package com.hookah.kek_hookah.infrastructure.config.security

import com.hookah.kek_hookah.feature.auth.JwtProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebFluxSecurity
open class SecurityConfig(
    private val jwtProvider: JwtProvider,
    private val jwtAuthenticationConverter: JwtAuthenticationConverter

) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeExchange {
                it.pathMatchers("/api/v1/auth/**").permitAll()
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtDecoder(reactiveJwtDecoder())
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                }
                oauth2.authenticationEntryPoint { exchange, _ ->
                    val path = exchange.request.path.value()
                    if (path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/auth/register")) {
                        exchange.response.statusCode = HttpStatus.OK
                        exchange.response.setComplete()
                    } else {
                        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                        exchange.response.setComplete()
                    }
                }

            }
            .build()
    }

    @Bean
    fun reactiveJwtDecoder(): ReactiveJwtDecoder {
        val signingKey = jwtProvider.getSigningKey()
        return NimbusReactiveJwtDecoder.withSecretKey(signingKey).build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedOrigins = listOf(
            "http://localhost:3000",
            "http://localhost:8080",
            "https://yourdomain.com"
        )
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        config.allowedHeaders = listOf("*")
        config.allowCredentials = true
        config.maxAge = 3600

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }
}
