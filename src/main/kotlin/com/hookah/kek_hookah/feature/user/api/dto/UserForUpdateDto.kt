package com.hookah.kek_hookah.feature.user.api.dto

import com.hookah.kek_hookah.infrastructure.validation.NullableNotBlank

data class UserForUpdateDto(
    @field:NullableNotBlank(message = "Name cannot be blank")
    val name: String? = null,

    @field:NullableNotBlank(message = "Email cannot be blank")
    val email: String? = null
)
