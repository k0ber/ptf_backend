package org.patifiner.relations.api

import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import org.koin.ktor.ext.inject
import org.patifiner.auth.JWT_AUTH
import org.patifiner.auth.getCurrentUserId
import org.patifiner.relations.RelationsService

fun Route.relationsRoutes() {
    val relationsService: RelationsService by inject()

    authenticate(JWT_AUTH) {
        get("/user/relations") {
            val userId = call.getCurrentUserId()
            val myRelations = relationsService.getMyRelations(userId)
            call.respond(myRelations)
        }

        post("/user/relations/status") {
            val userId = call.getCurrentUserId()
            val request = call.receive<UpdateRelationRequest>()
            val response = relationsService.sendInvite(userId, request.targetUserId)
            call.respond(response)
        }

        put("/user/relations/accept") {
            val userId = call.getCurrentUserId()
            val request = call.receive<UpdateRelationRequest>()
            val response = relationsService.acceptInvite(userId, request.targetUserId)
            call.respond(response)
        }

        post("/user/relations/block") {
            val userId = call.getCurrentUserId()
            val request = call.receive<UpdateRelationRequest>()
            val response = relationsService.blockUser(userId, request.targetUserId)
            call.respond(response)
        }
    }
}
