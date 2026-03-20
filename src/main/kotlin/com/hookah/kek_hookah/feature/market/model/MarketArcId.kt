package com.hookah.kek_hookah.feature.market.model

import com.hookah.kek_hookah.feature.common.model.EntityId
import com.hookah.kek_hookah.utils.uuid.uuidV7
import java.util.UUID

@JvmInline
value class MarketArcId(
    override val id: UUID = uuidV7()
) : EntityId {
    constructor(id: String) : this(UUID.fromString(id))
    override fun toString() = id.toString()
}
