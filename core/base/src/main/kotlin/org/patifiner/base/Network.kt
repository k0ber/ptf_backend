package org.patifiner.base

import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation


fun Application.installPtfSerialization() {
    install(ContentNegotiation) {
        jackson {
            configurePtfDefaults()
        }
    }
}
