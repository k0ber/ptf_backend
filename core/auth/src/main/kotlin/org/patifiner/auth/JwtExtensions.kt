package org.patifiner.auth

import com.auth0.jwt.JWT
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import org.patifiner.auth.exceptions.AuthException.InvalidTokenException
import java.util.Date

const val JWT_AUTH = "auth-jwt"
private const val USER_ID_FIELD_NAME = "id"

fun ApplicationCall.getCurrentUserId(): Long =
    principal<JWTPrincipal>()?.getClaim(USER_ID_FIELD_NAME, Long::class)
        ?: throw InvalidTokenException()

fun generateToken(jwtInfo: JwtConfig, userId: Long): String {
    return JWT.create()
        .withAudience(jwtInfo.audience)
        .withIssuer(jwtInfo.issuer)
        .withClaim(USER_ID_FIELD_NAME, userId)
        .withExpiresAt(Date(System.currentTimeMillis() + jwtInfo.expirationMs))
        .sign(jwtInfo.algorithm)
}

fun Application.installAuth(info: JwtConfig) {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT.require(info.algorithm)
                    .withAudience(info.audience)
                    .withIssuer(info.issuer)
                    .build()
            )
            realm = info.realm

            validate { credential ->
                if (credential.payload.getClaim(USER_ID_FIELD_NAME).asLong() == null) null
                else if (credential.payload.audience.contains(info.audience)) JWTPrincipal(
                    credential.payload
                )
                else null
            }

            challenge { _, _ -> throw InvalidTokenException() }
        }
    }
}
