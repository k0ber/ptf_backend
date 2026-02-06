package org.patifiner.auth

import io.ktor.server.config.ApplicationConfig
import org.koin.dsl.module
import org.patifiner.base.PtfJwtConfig

fun authModule(config: ApplicationConfig) = module {
    single<PtfJwtConfig> {
        with(config.config("jwt")) {
            PtfJwtConfig(
                secret = property("secret").getString(),
                audience = property("audience").getString(),
                realm = property("realm").getString(),
                issuer = property("issuer").getString(),
                accessTokenExpirationMs = property("expiration").getString().toLong(),
                refreshTokenExpirationMs = propertyOrNull("refreshExpiration")?.getString()?.toLong() ?: 2592000000L // 30 days
            )
        }
    }
}
