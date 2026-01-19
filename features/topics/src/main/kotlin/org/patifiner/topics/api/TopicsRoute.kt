package org.patifiner.topics.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import org.patifiner.auth.JWT_AUTH
import org.patifiner.auth.getCurrentUserId
import org.patifiner.topics.TopicsService
import org.slf4j.Logger


fun Route.topicsRoutes() {
    val topicsService: TopicsService by inject()
    val logger: Logger by inject()

    route("/topics") {
        authenticate(JWT_AUTH) {
            post("/create") {
                val request = call.receive<CreateTopicRequest>()
                val topic = topicsService.createTopic(request)
                call.respond(HttpStatusCode.Created, topic)
            }

            // search for autocomplete
            get("/search") {
                val query = call.request.queryParameters["q"] ?: ""
                val topics = topicsService.searchTopics(query)
                logger.debug("/topics/search/{} result ids: {}", query, topics.map { it.id })
                call.respond(topics)
            }

            // get topics tree
            get("/tree") {
                val tree = topicsService.getTopicsTree()
                call.respond(tree)
            }

            get("/{slug}") {
                val slug = call.parameters["slug"] ?: "" //return@get call.respond(HttpStatusCode.BadRequest)
                val topic = topicsService.getBySlug(slug)
                if (topic != null) call.respond(topic)
                else call.respond(HttpStatusCode.NotFound)
            }

            // add user topics
            post("/me") {
                val userId = call.getCurrentUserId()
                val request = call.receive<AddUserTopicsRequest>()
                val addedTopics = topicsService.addUserTopics(userId, request)
                call.respond(HttpStatusCode.Created, addedTopics)
            }

            // remove user topics
            delete("/me") {
                val userId = call.getCurrentUserId()
                val request = call.receive<RemoveUserTopicsRequest>()
                val removedCount = topicsService.removeUserTopics(userId, request.topicIds)
                call.respond(HttpStatusCode.OK, mapOf("removed" to removedCount))
            }

            // get user topics
            get("/me") {
                val userId = call.getCurrentUserId()
                val userTopics = topicsService.getUserTopics(userId)
                call.respond(userTopics)
            }
        }
    }

}
