package org.patifiner.search.api

import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject
import org.patifiner.auth.JWT_AUTH
import org.patifiner.auth.getCurrentUserId
import org.patifiner.search.SearchService

fun Route.searchRoutes() {

    val searchService: SearchService by inject()

    authenticate(JWT_AUTH) {
        post("/search/users") {
            val myId = call.getCurrentUserId()
            val pagingRequest = call.receive<PaginationRequest>()
            call.respond(searchService.findUsers(myId, pagingRequest))
        }

        get("/search/ideas") {
            val myId = call.getCurrentUserId()
            call.respond(searchService.findTopicIdea(myId))
        }
    }
}
