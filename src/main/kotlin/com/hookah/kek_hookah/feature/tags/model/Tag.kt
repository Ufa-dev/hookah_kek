package com.hookah.kek_hookah.feature.tags.model

import com.hookah.kek_hookah.feature.user.model.UserId
import java.time.OffsetDateTime

data class Tag(
    val id: TagId,
    val name: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val updatedBy: UserId
)
