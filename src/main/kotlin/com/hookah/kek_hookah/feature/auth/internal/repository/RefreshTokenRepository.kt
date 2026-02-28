package com.hookah.kek_hookah.feature.auth.internal.repository

import com.hookah.kek_hookah.feature.auth.model.JwtId
import com.hookah.kek_hookah.feature.auth.model.RefreshToken
import com.hookah.kek_hookah.feature.user.model.UserId
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.*

@Component

class RefreshTokenRepository(
    private val template: R2dbcEntityTemplate
) {
    suspend fun save(token: RefreshToken): RefreshToken {
        return template.insert(token.toEntity())
            .awaitSingle()
            .toRefreshToken()
    }

    suspend fun findByJti(jti: String): RefreshToken? {
        return template.select(RefreshTokenEntity::class.java)
            .matching(Query.query(where("jti").`is`(jti)))
            .awaitOneOrNull()
            ?.toRefreshToken()
    }

    suspend fun deleteByJti(jti: String): Boolean {
        return template.delete(RefreshTokenEntity::class.java)
            .matching(Query.query(where("jti").`is`(jti)))
            .all()
            .awaitSingle() > 0
    }

    suspend fun deleteExpired() {
        template.delete(RefreshTokenEntity::class.java)
            .matching(Query.query(where("expires_at").lessThan(OffsetDateTime.now())))
            .all()
            .awaitSingle()
    }

    private fun RefreshTokenEntity.toRefreshToken() = RefreshToken(
        id = JwtId(id),
        userId = UserId(userId),
        jti = jti,
        expiresAt = expiresAt,
        createdAt = createdAt
    )

    private fun RefreshToken.toEntity() = RefreshTokenEntity(
        id = id.id,
        userId = userId.id,
        jti = jti,
        expiresAt = expiresAt,
        createdAt = createdAt
    )

    @Table("refresh_tokens")
    data class RefreshTokenEntity(
        @Id
        @Column("id")
        val id: UUID,
        @Column("user_id")
        val userId: UUID,
        @Column("jti")
        val jti: String,
        @Column("expires_at")
        val expiresAt: OffsetDateTime,
        @Column("created_at")
        val createdAt: OffsetDateTime
    )
}