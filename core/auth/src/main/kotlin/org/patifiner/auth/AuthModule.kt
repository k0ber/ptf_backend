package org.patifiner.auth

import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.ApplicationConfig
import org.koin.dsl.module

data class JwtInfo(
    val secret: String,
    val audience: String,
    val realm: String,
    val issuer: String,
    val expirationMs: Long
) {
    val algorithm: Algorithm = Algorithm.HMAC256(secret)
}

fun authModule(config: ApplicationConfig) = module {
    val jwtInfo = with(config.config("jwt")) {
        JwtInfo(
            secret = property("secret").getString(),
            audience = property("audience").getString(),
            realm = property("realm").getString(),
            issuer = property("issuer").getString(),
            expirationMs = property("expiration").getString().toLong()
        )
    }
    single<JwtInfo> { jwtInfo }
}
