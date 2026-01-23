package org.patifiner.events.api

import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.patifiner.auth.JWT_AUTH
import org.patifiner.auth.getCurrentUserId
import org.patifiner.events.EventsService

fun Route.eventsRoutes() {
    val eventsService: EventsService by inject()

    route("/events") {
        authenticate(JWT_AUTH) {

            post("/create") {
                val userId = call.getCurrentUserId()
                val request = call.receive<CreateEventRequest>()
                call.respond(eventsService.createEvent(userId, request))
            }

            put("/{id}") {
                val userId = call.getCurrentUserId()
                val eventId = call.parameters["id"]?.toLongOrNull() ?: return@put
                val request = call.receive<UpdateEventRequest>()
                call.respond(eventsService.updateEvent(userId, eventId, request))
            }

            delete("/{id}") {
                val userId = call.getCurrentUserId()
                val eventId = call.parameters["id"]?.toLongOrNull() ?: return@delete
                eventsService.deleteEvent(userId, eventId)
                call.respond(io.ktor.http.HttpStatusCode.OK)
            }

            post("/{id}/join") {
                val userId = call.getCurrentUserId()
                val eventId = call.parameters["id"]?.toLongOrNull() ?: return@post
                call.respond(eventsService.joinEvent(userId, eventId))
            }

            post("/{id}/leave") {
                val userId = call.getCurrentUserId()
                val eventId = call.parameters["id"]?.toLongOrNull() ?: return@post
                call.respond(eventsService.leaveEvent(userId, eventId))
            }

            post("/{id}/invite") {
                val userId = call.getCurrentUserId()
                val eventId = call.parameters["id"]?.toLongOrNull() ?: return@post
                val request = call.receive<InviteParticipantRequest>()
                call.respond(eventsService.inviteUser(userId, eventId, request.targetUserId))
            }
        }
    }
}
