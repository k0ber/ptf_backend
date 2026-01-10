package org.patifiner.app

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import org.patifiner.base.PatifinerException
import org.slf4j.Logger

data class ErrorResponse(val code: String, val message: String)

fun Application.installStatusPages(logger: Logger) {
    install(StatusPages) {
        exception<PatifinerException> { call, cause ->
            logger.error("[${cause.code}] ${cause.message}")
            call.respond(cause.statusCode, ErrorResponse(cause.code, cause.message))
        }
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

