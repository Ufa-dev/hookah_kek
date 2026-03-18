package com.hookah.kek_hookah.feature.tobacco.pack.model

import com.hookah.kek_hookah.feature.common.model.EntityId
import com.hookah.kek_hookah.utils.uuid.uuidV7
import java.util.*

@JvmInline
value class PackId(val value: String) {
    override fun toString(): String = value
}
