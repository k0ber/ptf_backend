package org.patifiner.check.routes.getcheck

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.java.KoinJavaComponent.inject
import org.patifiner.check.data.CheckData
import org.slf4j.Logger


fun Route.getCheck() {
    val logger: Logger by inject(Logger::class.java)
    val service: CheckData by inject(CheckData::class.java)

    get("/check") {
        logger.info("check module loaded")
        call.respond(service.getCheckStatus())
    }
}
