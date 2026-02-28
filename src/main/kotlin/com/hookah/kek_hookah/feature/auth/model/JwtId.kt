package com.hookah.kek_hookah.feature.auth.model

import com.hookah.kek_hookah.feature.common.model.EntityId
import com.hookah.kek_hookah.utils.uuid.uuidV7
import java.util.*

@JvmInline
value class JwtId(
    override val id: UUID = uuidV7()
) : EntityId {

    constructor(id: String) : this(UUID.fromString(id))

    override fun toString(): String {
        return id.toString()
    }


}
