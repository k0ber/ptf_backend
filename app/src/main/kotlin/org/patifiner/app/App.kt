package org.patifiner.app

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.doublereceive.DoubleReceive
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.routing.routing
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Database
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.patifiner.auth.authModule
import org.patifiner.auth.installPtfAuth
import org.patifiner.base.PtfDbConfig
import org.patifiner.base.PtfJwtConfig
import org.patifiner.base.coreModule
import org.patifiner.base.installPtfSerialization
import org.patifiner.base.installPtfStatusPages
import org.patifiner.check.di.checkModule
import org.patifiner.check.routes.checkRoutes
import org.patifiner.database.DbInitializer
import org.patifiner.database.databaseModule
import org.patifiner.events.api.eventsRoutes
import org.patifiner.events.eventsModule
import org.patifiner.geo.api.geoRoutes
import org.patifiner.geo.geoModule
import org.patifiner.relations.api.relationsRoutes
import org.patifiner.relations.relationsModule
import org.patifiner.search.api.searchRoutes
import org.patifiner.search.searchModule
import org.patifiner.topics.api.topicsRoutes
import org.patifiner.topics.topicsModule
import org.patifiner.upload.api.uploadRoutes
import org.patifiner.upload.uploadModule
import org.patifiner.user.api.userRoutes
import org.patifiner.user.userModule
import org.slf4j.Logger
import org.slf4j.event.Level

private const val CORS_HOST = "patifiner.ru"

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // application.conf module
internal fun Application.module() {
    val config = environment.config

    //region DI
    install(Koin) {
        slf4jLogger()
        modules(
            coreModule,
            authModule(config),
            checkModule(config),
            databaseModule(config),
            uploadModule(config),
            userModule,
            topicsModule,
            searchModule,
            geoModule,
            eventsModule,
            relationsModule,
        )
    }

    val logger: Logger by inject()
    val jwtConfig: PtfJwtConfig by inject()
    val databaseConfig: PtfDbConfig by inject()
    val databaseInitializer: DbInitializer by inject()
    //endregion

    initPtfDatabase(databaseConfig, databaseInitializer, logger)

    installPtfAuth(jwtConfig)
    installPtfStatusPages(logger)
    installPtfSerialization()
    installPtfCallLogs()
    installPtfCors()

    routing {
        checkRoutes()
        userRoutes()
        topicsRoutes()
        searchRoutes()
        uploadRoutes()
        geoRoutes()
        eventsRoutes()
        relationsRoutes()
    }

    logger.info("Server has started")
}

private fun Application.installPtfCallLogs() {
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

private fun Application.installPtfCors() {
    install(CORS) {
        allowHost(CORS_HOST, schemes = listOf("https"))

        allowHeader(io.ktor.http.HttpHeaders.Authorization)
        allowHeader(io.ktor.http.HttpHeaders.ContentType)

        allowMethod(io.ktor.http.HttpMethod.Options)
        allowMethod(io.ktor.http.HttpMethod.Put)
        allowMethod(io.ktor.http.HttpMethod.Delete)
        allowMethod(io.ktor.http.HttpMethod.Patch)

        allowCredentials = true
    }
}

private fun Application.initPtfDatabase(dbConfig: PtfDbConfig, databaseInitializer: DbInitializer, logger: Logger) {
    with(dbConfig) {
        logger.info("Connecting to database at $url")
        Database.connect(url, driver, user, password)
    }
    launch {
        runCatching { databaseInitializer.initData() }
            .onSuccess { logger.info("Database initialization completed successfully") }
            .onFailure { e -> logger.error("Failed to initialize database", e) }
    }
}
