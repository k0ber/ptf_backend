package org.patifiner.geo

import org.patifiner.database.tables.CityEntity

data class CityDto(
    val id: Long,
    val name: String,
    val country: String
)

fun CityEntity.toDto() = CityDto(
    id = id.value,
    name = name,
    country = country
)
