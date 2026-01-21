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
import org.koin.ktor.ext.inject
import org.patifiner.auth.JWT_AUTH
import org.patifiner.auth.getCurrentUserId
import org.patifiner.base.readRawFile
import org.patifiner.upload.UploadService
import org.patifiner.user.UserService
import org.slf4j.Logger

fun Route.userRoutes() {

    val userService: UserService by inject()
    val uploadService: UploadService by inject()
    val logger: Logger by inject()

    post("/user/create") {
        logger.info("new user request")
        val request = call.receive<CreateUserRequest>()
        val response: UserCreatedResponse = userService.createUser(request)
        call.respond(response)
    }

    post("/user/login") {
        val request = call.receive<TokenRequest>()
        val tokenResponse = userService.requestToken(request.email, request.password)
        call.respond(tokenResponse)
    }

    post("/user/refresh") {
        val request = call.receive<RefreshTokenRequest>()
        val tokenResponse = userService.refreshToken(request.refreshToken)
        call.respond(tokenResponse)
    }

    authenticate(JWT_AUTH) {
        get("/user/me") {
            val myUserId = call.getCurrentUserId()
            val myUserInfo = userService.getUserInfo(myUserId)
            call.respond(myUserInfo)
        }

        put("/user/update") {
            val userId = call.getCurrentUserId()
            val request = call.receive<UpdateUserRequest>()
            val updatedUser = userService.updateProfile(userId, request)
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
            val updatedUser = userService.updateAvatar(userId, request.url)
            call.respond(updatedUser)
        }

        put("/user/me/city") {
            val userId = call.getCurrentUserId()
            val request = call.receive<UpdateUserRequest>() // Используем общую модель
            val updatedUser = userService.updateProfile(userId, request)
            call.respond(updatedUser)
        }
    }
}
