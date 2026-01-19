package org.patifiner.base

data class PagedRequest(val page: Int, val perPage: Int)

data class PagedResponse<T>(
    val items: List<T>,
    val page: Int,
    val perPage: Int,
    val total: Long
)

fun calculateOffset(page: Int, perPage: Int): Long {
    return ((page - 1).coerceAtLeast(0) * perPage.coerceIn(1, 100)).toLong()
}
