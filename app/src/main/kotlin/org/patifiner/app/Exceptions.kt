package org.patifiner.app

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import org.patifiner.auth.exceptions.AuthException.InvalidTokenException
import org.patifiner.search.api.SearchException
import org.patifiner.topics.api.TopicsException.InvalidTopicName
import org.patifiner.topics.api.TopicsException.TopicNotFoundException
import org.patifiner.topics.api.TopicsException.UserTopicAlreadyExistsException
import org.patifiner.upload.api.UploadException
import org.patifiner.user.api.UserException.EmailAlreadyTakenException
import org.patifiner.user.api.UserException.InvalidCredentialsException
import org.patifiner.user.api.UserException.UserNotFoundByEmailException
import org.patifiner.user.api.UserException.UserNotFoundByIdException
import org.slf4j.Logger


fun Application.installStatusPages(logger: Logger) {
    install(StatusPages) {
        registerAuthExceptions(logger)
        registerUserExceptions(logger)
        registerTopicsExceptions(logger)
        registerSearchExceptions(logger)
        registerUploadExceptions(logger)
        exception<BadRequestException> { call, cause ->
            logger.error("BadRequest: ${cause.message}", cause)
            call.respondText("Bad Request error: ${cause.message}", status = HttpStatusCode.BadRequest)
        }
        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception: ${cause.message}", cause)
            call.respondText("Internal server error", status = HttpStatusCode.InternalServerError)
        }
    }
}

data class ErrorResponse(val code: String, val message: String)
// todo: refactor exceptions

fun StatusPagesConfig.registerAuthExceptions(logger: Logger) {
    exception<InvalidTokenException> { call, cause ->
        logger.error("Unhandled exception: ${cause.message}", cause)
        call.respond(HttpStatusCode.Unauthorized, ErrorResponse("auth", "Invalid or expired token"))
    }
}

fun StatusPagesConfig.registerUserExceptions(logger: Logger) {
    exception<UserNotFoundByEmailException> { call, cause ->
        logger.error("Unhandled exception: ${cause.message}", cause)
        call.respond(HttpStatusCode.NotFound, ErrorResponse("register", cause.message))
    }

    exception<UserNotFoundByIdException> { call, cause ->
        logger.error("Unhandled exception: ${cause.message}", cause)
        call.respond(HttpStatusCode.NotFound, ErrorResponse("register", cause.message))
    }

    exception<InvalidCredentialsException> { call, cause ->
        logger.error("Unhandled exception: ${cause.message}", cause)
        call.respond(HttpStatusCode.Unauthorized, ErrorResponse("register", cause.message))
    }

    exception<EmailAlreadyTakenException> { call, cause ->
        logger.error("Unhandled exception: ${cause.message}", cause)
        call.respond(HttpStatusCode.Conflict, ErrorResponse("register", cause.message))
    }
}


fun StatusPagesConfig.registerTopicsExceptions(logger: Logger) {
    exception<InvalidTopicName> { call, cause ->
        logger.error("Topic error: ${cause.message}", cause)
        call.respond(HttpStatusCode.BadRequest, ErrorResponse("topic", cause.message ?: "This name is not allowed"))
    }
    exception<TopicNotFoundException> { call, cause ->
        logger.error("Topic error: ${cause.message}", cause)
        call.respond(HttpStatusCode.NotFound, ErrorResponse("topic", cause.message ?: "Topic not found"))
    }
    exception<UserTopicAlreadyExistsException> { call, cause ->
        logger.error("Topic error: ${cause.message}", cause)
        call.respond(HttpStatusCode.Conflict, ErrorResponse("topic", cause.message ?: "User topic already exists"))
    }
}


fun StatusPagesConfig.registerSearchExceptions(logger: Logger) {
    exception<SearchException> { call, cause ->
        logger.error("Unhandled exception: ${cause.message}", cause)
        call.respond(HttpStatusCode.NotFound, ErrorResponse("search", cause.message ?: "search service failed"))
    }
}

fun StatusPagesConfig.registerUploadExceptions(logger: Logger) {
    exception<UploadException> { call, cause ->
        logger.error("Upload error: ${cause.message}", cause)
        call.respond(cause.statusCode, ErrorResponse("user", cause.message))
    }
}
