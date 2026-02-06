package org.patifiner.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.server.config.ApplicationConfig
import org.koin.dsl.module
import org.patifiner.base.PtfDbConfig

fun databaseModule(config: ApplicationConfig) = module {
    single<PtfDbConfig> {
        with(config.config("database")) {
            PtfDbConfig(
                url = property("url").getString(),
                driver = property("driver").getString(),
                user = property("user").getString(),
                password = property("password").getString()
            )
        }
    }

    single { DbInitializer(logger = get()) }
}

object DB {
    val jackson: ObjectMapper = jacksonObjectMapper().findAndRegisterModules()
}
