package com.hookah.kek_hookah.feature.user.internal.repository

import com.hookah.kek_hookah.feature.user.model.User
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
class UserRepository(
    private val template: R2dbcEntityTemplate
) {

    //todo maybe use just one method?? but maybe its only methods we will be use
    suspend fun findById(id: UserId): User? {
        return template.select(UserEntity::class.java)
            .matching(
                Query.query(
                    where("id").`is`(id.id)
                )
            ).awaitOneOrNull()?.toUser()
    }

    suspend fun findByEmail(email: String): User? {
        return template.select(UserEntity::class.java)
            .matching(
                Query.query(
                    where("email").`is`(email)
                )
            ).awaitOneOrNull()?.toUser()
    }

    suspend fun insert(user: User): User {
        return template.insert(user.toEntity()).awaitSingle().toUser()
    }

    suspend fun update(user: User): User {
        return template.update(user.toEntity()).awaitSingle().toUser()
    }

    private fun UserEntity.toUser() = User(
        id = UserId(id),
        name = name,
        email = email,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun User.toEntity() = UserEntity(
        id = id.id,
        name = name,
        email = email,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    @Table("users")
    data class UserEntity(
        @Id
        val id: UUID,

        @Column("name")
        val name: String,

        @Column("email")
        val email: String,

        @Column("created_at")
        val createdAt: OffsetDateTime,

        @Column("updated_at")
        val updatedAt: OffsetDateTime,
    )

}