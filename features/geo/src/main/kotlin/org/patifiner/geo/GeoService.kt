package org.patifiner.geo

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.patifiner.base.PagedRequest
import org.patifiner.base.PagedResponse
import org.patifiner.base.calculateOffset
import org.slf4j.Logger

class GeoService(
    private val geoDao: GeoDao,
    private val logger: Logger
) {
    suspend fun searchCities(query: String): List<CityDto> = newSuspendedTransaction(Dispatchers.IO) {
        if (query.isBlank()) return@newSuspendedTransaction emptyList()
        logger.debug("Searching cities with query: $query")
        geoDao.searchCities(query)
    }

    suspend fun getCities(request: PagedRequest): PagedResponse<CityDto> = newSuspendedTransaction(Dispatchers.IO) {
        val safePerPage = request.perPage.coerceIn(1, 100)
        val offset = calculateOffset(request.page, safePerPage)

        val items = geoDao.getCities(safePerPage, offset)
        val total = geoDao.getTotalCitiesCount()

        return@newSuspendedTransaction PagedResponse(
            items = items,
            page = request.page,
            perPage = safePerPage,
            total = total
        )
    }
}
