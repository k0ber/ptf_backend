package org.patifiner.app

import io.ktor.server.config.ApplicationConfig
import org.koin.dsl.module
import org.patifiner.upload.api.UploadConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal data class DatabaseConfig(
    val url: String,
    val driver: String,
    val user: String,
    val password: String,
)

internal fun appModule(config: ApplicationConfig) = module {
    single<Logger> { LoggerFactory.getLogger("App") }
    single<DatabaseConfig> {
        with(config.config("database")) {
            DatabaseConfig(
                url = property("url").getString(),
                driver = property("driver").getString(),
                user = property("user").getString(),
                password = property("password").getString()
            )
        }
    }
    single<UploadConfig> {
        with(config.config("upload")) {
            UploadConfig(
                uploadPath = property("path").getString(),
                baseUrl = property("baseUrl").getString(),
                maxFileSizeMB = property("maxSize").getString().toInt()
            )
        }
    }
}
