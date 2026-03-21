package com.hookah.kek_hookah.feature.tobacco.brand.internal.repository

import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
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
class BrandRepository(
    private val template: R2dbcEntityTemplate
) {

    suspend fun findById(id: BrandId): TabacoBrand? {
        return template.select(BrandEntity::class.java)
            .matching(
                Query.query(
                    where("id").`is`(id.id)
                )
            ).awaitOneOrNull()?.toBrand()

    }

    suspend fun findByName(name: String): TabacoBrand? {
        return template.select(BrandEntity::class.java)
            .matching(
                Query.query(
                    where("name").`is`(name)
                )
            ).awaitOneOrNull()?.toBrand()
    }

    suspend fun findAllByName(name: String): List<TabacoBrand> {
        return template.select(BrandEntity::class.java)
            .matching(
                Query.query(
                    where("name").like("%$name%")
                )
            )
            .all()
            .collectList()
            .awaitSingle()
            .map { it.toBrand() }
    }

    suspend fun findAll(limit: Int, afterId: UUID?): List<TabacoBrand> {
        val query = if (afterId != null) {
            Query.query(where("id").greaterThan(afterId))
        } else {
            Query.empty()
        }
        return template.select(BrandEntity::class.java)
            .matching(query.sort(Sort.by(Sort.Direction.ASC, "id")).limit(limit))
            .all()
            .collectList()
            .awaitSingle()
            .map { it.toBrand() }
    }

    suspend fun insert(brand: TabacoBrand): TabacoBrand {
        return template.insert(brand.toEntity()).awaitSingle().toBrand()
    }

    suspend fun update(brand: TabacoBrand): TabacoBrand {
        return template.update(brand.toEntity()).awaitSingle().toBrand()
    }

    suspend fun delete(id: BrandId) {
        template.delete(BrandEntity::class.java)
            .matching(Query.query(where("id").`is`(id.id)))
            .all()
            .awaitSingle()
    }

    private fun BrandEntity.toBrand() = TabacoBrand(
        id = BrandId(id),
        name = name,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
        updatedBy = UserId(updatedBy)
    )

    private fun TabacoBrand.toEntity() = BrandEntity(
        id = id.id,
        name = name,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
        updatedBy = updatedBy.id,
    )


    @Table("tabacoo_brand")
    data class BrandEntity(
        @Id
        val id: UUID,

        @Column("name")
        val name: String,

        @Column("description")
        val description: String?,

        @Column("created_at")
        val createdAt: OffsetDateTime,

        @Column("updated_at")
        val updatedAt: OffsetDateTime,

        @Column("updated_by")
        val updatedBy: UUID,
    )

}
