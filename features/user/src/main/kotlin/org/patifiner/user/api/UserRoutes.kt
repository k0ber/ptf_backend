package org.patifiner.user.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
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
import org.patifiner.base.readRawFile
import org.patifiner.database.UserTable
import org.patifiner.upload.UploadService
import org.patifiner.user.UserService
import org.slf4j.Logger

fun Route.userRoutes(scope: CoroutineScope) {

    val userService: UserService by inject()
    val uploadService: UploadService by inject()
    val logger: Logger by inject()

    // todo: перенести в отдельный DatabaseInitializer
    scope.launch(Dispatchers.IO) {
        newSuspendedTransaction {
            SchemaUtils.create(UserTable)
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

        post("/user/me/photo") {
            val userId = call.getCurrentUserId()
            val multipart = call.receiveMultipart()
            val rawFile = multipart.readRawFile() ?: throw UserException.FileIsMissing()

            val response = uploadService.saveImage(
                fileBytes = rawFile.bytes,
                originalFileName = rawFile.name,
                fileContentType = rawFile.contentType,
                fileSize = rawFile.bytes.size.toLong()
            )

            val updatedUser = userService.addPhoto(userId, response.url)
            call.respond(HttpStatusCode.Created, updatedUser)
        }

        delete("/user/me/photo") {
            val userId = call.getCurrentUserId()
            val request = call.receive<DeletePhotoRequest>()
            val photoUrl = request.url
            userService.removePhoto(userId, photoUrl)

            val fileName = photoUrl.substringAfterLast("/")
            uploadService.deleteImage(fileName)
            call.respond(HttpStatusCode.OK, userService.getUserInfo(userId))
        }

        put("/user/me/avatar") {
            val userId = call.getCurrentUserId()
            val request = call.receive<SetMainPhotoRequest>()

            val updatedUser = userService.setMainPhoto(userId, request.url)
            call.respond(updatedUser)
        }
    }
}
