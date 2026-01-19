package org.patifiner.check.di

import io.ktor.server.config.ApplicationConfig
import org.koin.dsl.bind
import org.koin.dsl.module
import org.patifiner.check.data.CheckData
import org.patifiner.check.data.CheckDataImpl

data class CheckConfig(
    val versionName: String,
    val versionCode: String
)

fun checkModule(config: ApplicationConfig) = module {
    val checkConfig = with(config.config("version")) {
        CheckConfig(
            versionName = propertyOrNull("name")?.getString() ?: "unknown",
            versionCode = propertyOrNull("code")?.getString() ?: "0"
        )
    }

    single { CheckDataImpl(checkConfig.versionName, checkConfig.versionCode) } bind CheckData::class
}
