package com.hookah.kek_hookah.utils.crud

data class Slice<T>(
    val items: List<T>,
    val nextToken: String?
) : Iterable<T> by items {

    fun <V> transform(transform: (T) -> V): Slice<V> = Slice(
        items = items.map(transform),
        nextToken = nextToken
    )
}
