package com.hookah.kek_hookah.feature.auth.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(

    @JsonProperty("email")
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Size(max = 255, message = "Email too long")
    val email: String,

    @JsonProperty("name")
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name too long")
    val name: String,

    @JsonProperty("password")
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 32, message = "Password must be 6-32 characters")
    val password: String
)