package org.patifiner.geo

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.patifiner.database.CitiesTable
import org.patifiner.database.CityEntity

interface GeoDao {
    suspend fun searchCities(query: String): List<CityDto>
    suspend fun getCities(limit: Int, offset: Long): List<CityDto>
    suspend fun getTotalCitiesCount(): Long
}

class ExposedGeoDao : GeoDao {
    override suspend fun searchCities(query: String): List<CityDto> = newSuspendedTransaction(Dispatchers.IO) {
        CityEntity.find { CitiesTable.name.lowerCase() like "${query.lowercase()}%" }
            .limit(20)
            .map { it.toDto() }
    }

    override suspend fun getCities(limit: Int, offset: Long): List<CityDto> = newSuspendedTransaction(Dispatchers.IO) {
        CityEntity.all()
            .limit(limit, offset)
            .map { it.toDto() }
    }

    override suspend fun getTotalCitiesCount(): Long = newSuspendedTransaction(Dispatchers.IO) {
        CityEntity.count()
    }
}
