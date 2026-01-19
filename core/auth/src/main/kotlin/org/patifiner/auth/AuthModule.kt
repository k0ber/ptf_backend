package org.patifiner.auth

import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.ApplicationConfig
import org.koin.dsl.module

data class JwtConfig(
    val secret: String,
    val audience: String,
    val realm: String,
    val issuer: String,
    val accessTokenExpirationMs: Long,
    val refreshTokenExpirationMs: Long
) {
    val algorithm: Algorithm = Algorithm.HMAC256(secret)
}

fun authModule(config: ApplicationConfig) = module {
    val jwtConfig = with(config.config("jwt")) {
        JwtConfig(
            secret = property("secret").getString(),
            audience = property("audience").getString(),
            realm = property("realm").getString(),
            issuer = property("issuer").getString(),
            accessTokenExpirationMs = property("expiration").getString().toLong(),
            refreshTokenExpirationMs = propertyOrNull("refreshExpiration")?.getString()?.toLong() ?: 2592000000L // 30 days
        )
    }
    single<JwtConfig> { jwtConfig }
}
