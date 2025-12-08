package org.patifiner.app

import io.ktor.server.config.ApplicationConfig
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory


internal data class DatabaseInfo(
    val url: String,
    val driver: String,
    val user: String,
    val password: String,
)

internal fun appModule(config: ApplicationConfig) = module {
    single<Logger> { LoggerFactory.getLogger("App") }
    single<DatabaseInfo> {
        with(config.config("database")) {
            DatabaseInfo(
                url = property("url").getString(),
                driver = property("driver").getString(),
                user = property("user").getString(),
                password = property("password").getString()
            )
        }
    }
}
