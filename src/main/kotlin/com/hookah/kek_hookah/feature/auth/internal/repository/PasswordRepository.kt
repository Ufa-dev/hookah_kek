package com.hookah.kek_hookah.feature.auth.internal.repository

import com.hookah.kek_hookah.feature.auth.model.Password
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
class PasswordRepository(
    private val template: R2dbcEntityTemplate
) {

    suspend fun findByUserId(userId: UserId): Password? {
        return template.select(PasswordEntity::class.java)
            .matching(
                Query.query(
                    where("user_id").`is`(userId.id)
                )
            )
            .awaitOneOrNull()
            ?.toPassword()
    }

    suspend fun insert(password: Password): Password {
        return template.insert(password.toEntity())
            .awaitSingle().toPassword()
    }

    suspend fun update(password: Password): Password {
        return template.update(password.toEntity())
            .awaitSingle().toPassword()
    }

    private fun PasswordEntity.toPassword() = Password(
        userId = UserId(userId),
        password = password,
        generatedAt = generatedAt
    )

    private fun Password.toEntity() = PasswordEntity(
        userId = userId.id,
        password = password,
        generatedAt = generatedAt
    )

    @Table("passwords")
    data class PasswordEntity(
        @Id
        @Column("user_id")
        val userId: UUID,

        @Column("password")
        val password: String,

        @Column("generated_at")
        val generatedAt: OffsetDateTime
    )
}