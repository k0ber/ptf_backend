package org.patifiner.check.routes

import io.ktor.server.routing.Route
import org.patifiner.check.routes.getcheck.getCheck

fun Route.checkRoutes() {
    getCheck()
}
