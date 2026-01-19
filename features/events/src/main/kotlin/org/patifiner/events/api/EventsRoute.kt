package org.patifiner.events.api

import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import org.patifiner.auth.JWT_AUTH
import org.patifiner.events.EventsService

fun Route.eventsRoutes() {

    val eventsService: EventsService by inject()

    route("/events") {
        authenticate(JWT_AUTH) {
            post("/create_new") {
                // todo: понять что нужно в роуте ивентов
            }
        }
    }

}
