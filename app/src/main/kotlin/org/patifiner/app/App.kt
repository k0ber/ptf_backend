package org.patifiner.app

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule.Builder
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.doublereceive.DoubleReceive
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.Database
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.patifiner.auth.JwtInfo
import org.patifiner.auth.authModule
import org.patifiner.auth.installAuth
import org.patifiner.check.di.checkModule
import org.patifiner.check.routes.checkRoutes
import org.patifiner.search.api.searchRoutes
import org.patifiner.search.searchModule
import org.patifiner.topics.api.topicsRoute
import org.patifiner.topics.topicsModule
import org.patifiner.upload.api.UploadConfig
import org.patifiner.upload.api.uploadRoutes
import org.patifiner.upload.uploadModule
import org.patifiner.user.api.userRoutes
import org.patifiner.user.userModule
import org.slf4j.Logger
import org.slf4j.event.Level

private val uploadConfig = UploadConfig(
    uploadPath = "files/uploads",
    baseUrl = "http://localhost:8080/files",
    maxFileSizeMB = 5,
)

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // application.conf module
internal fun Application.module() {
    val config = environment.config

    install(Koin) {
        slf4jLogger()
        modules(
            appModule(config),
            checkModule,
            authModule(config),
            userModule,
            topicsModule,
            searchModule,
            uploadModule(uploadConfig)
        )
    }

    val jwtInfo: JwtInfo by inject()
    val logger: Logger by inject()
    val databaseInfo: DatabaseInfo by inject()

    installAuth(jwtInfo)
    installStatusPages(logger)

    with(databaseInfo) {
        logger.info("Connecting to database at $url")
        Database.connect(url, driver, user, password) // NOTE: Реальная проверка соединения происходит здесь.
    }

    installJackson()
    installDebugPlugins()

    routing {
        checkRoutes()
        userRoutes(scope = this.application)
        topicsRoute(scope = this.application)
        searchRoutes()
        uploadRoutes()
    }

    logger.info("Server has started")
}

private fun Application.installJackson() {
    install(ContentNegotiation) {
        jackson {
            enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT)
            disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
            registerModule(JavaTimeModule())    // LocalDate/LocalDateTime
            registerModule(Builder().build())   // Kotlin data classes
        }
    }
}

private fun Application.installDebugPlugins() {
    // todo: Disable in prod
    install(DoubleReceive)
    install(CallLogging) {
        level = Level.INFO
        format { call ->
            val status = call.response.status() ?: "Unhandled"
            val httpMethod = call.request.httpMethod.value
            val uri = call.request.uri
            "$status: $httpMethod $uri"
        }
    }
}
