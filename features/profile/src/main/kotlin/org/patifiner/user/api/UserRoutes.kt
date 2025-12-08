package org.patifiner.user.api

import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.ktor.ext.inject
import org.patifiner.auth.JWT_AUTH
import org.patifiner.auth.getCurrentUserId
import org.patifiner.user.UserService
import org.patifiner.user.data.UsersTable
import org.slf4j.Logger

fun Route.userRoutes(scope: CoroutineScope) {

    val userService: UserService by inject()
    val logger: Logger by inject()

    scope.launch(Dispatchers.IO) {
        newSuspendedTransaction {
            SchemaUtils.create(UsersTable)
        }
    }

    post("/user/create") {
        val request = call.receive<CreateUserRequest>()
        logger.info("request: $request")
        val response: UserCreatedResponse = userService.createUser(request)
        call.respond(response)
    }

    post("/user/login") {
        val request = call.receive<TokenRequest>()
        val token = userService.requestToken(request.email, request.password)
        call.respond(TokenResponse(token))
    }

    authenticate(JWT_AUTH) {
        get("/user/me") {
            val myUserInd = call.getCurrentUserId()
            val myUserInfo = userService.getUserInfo(myUserInd)
            call.respond(myUserInfo)
        }

        put("/user/update") {
            val userId = call.getCurrentUserId()
            val request = call.receive<UpdateUserRequest>()
            val updatedUser = userService.updateAvatarUrl(
                userId = userId,
                avatarUrl = request.avatarUrl
            )
            call.respond(updatedUser)
        }
    }
}
