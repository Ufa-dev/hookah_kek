package com.hookah.kek_hookah.utils.crud

interface Queryable<Type, Spec> {

    suspend fun list(
        token: String? = null,
        specs: Set<Spec>,
        limit: Int,
    ): Slice<Type>

    suspend fun firstOrNull(
        vararg specs: Spec
    ): Type? = list(
        token = null,
        specs = specs.toSet(),
        limit = 1
    ).firstOrNull()

}
