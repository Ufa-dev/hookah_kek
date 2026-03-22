package com.maverick.landcruiser.server.e2e.user

import com.hookah.kek_hookah.feature.tobacco.e2e.auth.AuthorizedWebTestClient
import com.hookah.kek_hookah.feature.user.api.dto.UserForUpdateDto
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

const val ME_URL = "/api/v1/users/me"

fun AuthorizedWebTestClient.getMe(): WebTestClient.ResponseSpec {
    return get()
        .uri(ME_URL)
        .exchange()
}

fun AuthorizedWebTestClient.patchMe(request: UserForUpdateDto): WebTestClient.ResponseSpec {
    return patch()
        .uri(ME_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
}
