package org.patifiner.geo.api

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.patifiner.base.PagedRequest
import org.patifiner.geo.GeoService

fun Route.geoRoutes() {
    val geoService: GeoService by inject()

    route("/geo") {
        // Поиск городов через query parameter "q"
        get("/cities/search") {
            val query = call.request.queryParameters["q"] ?: ""
            val cities = geoService.searchCities(query)
            call.respond(cities)
        }

        // Получение всех городов с пагинацией через POST PagedRequest
        post("/cities/all") {
            val request = call.receive<PagedRequest>()
            val response = geoService.getCities(request)
            call.respond(response)
        }
    }
}
