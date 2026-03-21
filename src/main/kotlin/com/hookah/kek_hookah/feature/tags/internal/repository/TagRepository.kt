package com.hookah.kek_hookah.feature.tags.internal.repository

import com.hookah.kek_hookah.feature.tags.model.Tag
import com.hookah.kek_hookah.feature.tags.model.TagId
import com.hookah.kek_hookah.feature.user.model.UserId
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.*

@Component
class TagRepository(
    private val template: R2dbcEntityTemplate
) {
    suspend fun findByName(name: String): Tag? {
        return template.select(TagEntity::class.java)
            .matching(
                Query.query(
                    where("name").`is`(name)
                )
            ).awaitOneOrNull()?.toTag()
    }

    suspend fun findById(id: TagId): Tag? {
        return template.select(TagEntity::class.java)
            .matching(
                Query.query(
                    where("id").`is`(id.id)
                )
            ).awaitOneOrNull()?.toTag()
    }

    suspend fun findAll(limit: Int, afterId: UUID?): List<Tag> {
        val query = if (afterId != null) {
            Query.query(where("id").greaterThan(afterId))
        } else {
            Query.empty()
        }
        return template.select(TagEntity::class.java)
            .matching(query.sort(Sort.by(Sort.Direction.ASC, "id")).limit(limit))
            .all()
            .collectList()
            .awaitSingle()
            .map { it.toTag() }
    }

    suspend fun findAllByIds(ids: List<UUID>): List<Tag> {
        if (ids.isEmpty()) return emptyList()
        return template.select(TagEntity::class.java)
            .matching(Query.query(where("id").`in`(ids)))
            .all()
            .collectList()
            .awaitSingle()
            .map { it.toTag() }
    }

    suspend fun insert(tag: Tag): Tag {
        return template.insert(tag.toEntity()).awaitSingle().toTag()
    }

    suspend fun update(tag: Tag): Tag {
        return template.update(tag.toEntity()).awaitSingle().toTag()
    }

    private fun TagEntity.toTag() = Tag(
        id = TagId(id),
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        updatedBy = UserId(updatedBy),
    )

    private fun Tag.toEntity() = TagEntity(
        id = id.id,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        updatedBy = updatedBy.id,
    )

    @Table("tags")
    data class TagEntity(
        @Id
        val id: UUID,

        @Column("name")
        val name: String,

        @Column("created_at")
        val createdAt: OffsetDateTime,

        @Column("updated_at")
        val updatedAt: OffsetDateTime,

        @Column("updated_by")
        val updatedBy: UUID,
    )

}