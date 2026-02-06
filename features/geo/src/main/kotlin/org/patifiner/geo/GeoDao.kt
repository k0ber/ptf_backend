package org.patifiner.geo

import org.jetbrains.exposed.sql.lowerCase
import org.patifiner.database.tables.CitiesTable
import org.patifiner.database.tables.CityEntity

interface GeoDao {
    fun searchCities(query: String): List<CityDto>
    fun getCities(limit: Int, offset: Long): List<CityDto>
    fun getTotalCitiesCount(): Long
}

class ExposedGeoDao : GeoDao {

    override fun searchCities(query: String): List<CityDto> =
        CityEntity.find { CitiesTable.name.lowerCase() like "${query.lowercase()}%" }
            .limit(20)
            .map { it.toDto() }

    override fun getCities(limit: Int, offset: Long): List<CityDto> =
        CityEntity.all()
            .limit(limit, offset)
            .map { it.toDto() }

    override fun getTotalCitiesCount(): Long = CityEntity.count()

}
