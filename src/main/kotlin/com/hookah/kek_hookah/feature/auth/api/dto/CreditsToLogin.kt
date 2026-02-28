package com.hookah.kek_hookah.feature.auth.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class CreditsToLogin(
    @JsonProperty("email")
    val email: String,

    @JsonProperty("password")
    val password: String
)


