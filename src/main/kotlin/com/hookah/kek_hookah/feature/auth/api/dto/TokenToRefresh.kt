package com.hookah.kek_hookah.feature.auth.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TokenToRefresh(
    @JsonProperty("token")
    val token: String
)