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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.ktor.ext.inject
import org.patifiner.auth.JWT_AUTH
import org.patifiner.auth.getCurrentUserId
import org.patifiner.topics.TopicsService
import org.patifiner.topics.data.TopicsTable
import org.patifiner.topics.data.UserTopicsTable
import org.slf4j.Logger

private const val TOPICS_YAML = "/topics.yaml"

fun Route.topicsRoute(scope: CoroutineScope) {
    val topicsService: TopicsService by inject()
    val logger: Logger by inject()

    scope.launch(Dispatchers.IO) {
        newSuspendedTransaction {
            exec("CREATE EXTENSION IF NOT EXISTS pg_trgm") // required for search like
            logger.info("PostgreSQL extension 'pg_trgm' ensured to be created.")

            SchemaUtils.create(TopicsTable)
            SchemaUtils.create(UserTopicsTable)
        }

        val resourceStream = object {}.javaClass.getResourceAsStream(TOPICS_YAML) ?: throw RuntimeException("topics.yaml not found in resources")
        topicsService.importFromYaml(resourceStream.bufferedReader(Charsets.UTF_8).readText())
        logger.info("Topics imported from $TOPICS_YAML")
    }

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
