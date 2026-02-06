package org.patifiner.upload

import io.ktor.server.config.ApplicationConfig
import org.koin.dsl.module
import org.patifiner.base.PtfUploadConfig

fun uploadModule(config: ApplicationConfig) = module {
    single<PtfUploadConfig> {
        with(config.config("upload")) {
            PtfUploadConfig(
                uploadPath = property("path").getString(),
                baseUrl = property("baseUrl").getString(),
                maxFileSizeMB = property("maxSize").getString().toInt()
            )
        }
    }

    single { UploadService(config = get(), logger = get()) }
}
