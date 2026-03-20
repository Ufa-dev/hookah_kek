package com.hookah.kek_hookah.feature.tobacco.flavor

import com.hookah.kek_hookah.feature.tobacco.flavor.api.dto.FlavorSearchDto
import org.springframework.stereotype.Service

@Service
class FlavorService {

    /** Stub — full implementation coming soon. Returns empty list until flavors CRUD is built. */
    suspend fun search(q: String, limit: Int): List<FlavorSearchDto> = emptyList()

    /** Stub — returns null (404) until flavors CRUD is built. */
    suspend fun findById(id: String): FlavorSearchDto? = null
}
