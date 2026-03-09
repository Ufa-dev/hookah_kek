package com.hookah.kek_hookah.feature.tags.model

import com.hookah.kek_hookah.feature.user.model.UserId

data class TagForCreate(
    val name: String,
    val userId: UserId
)
