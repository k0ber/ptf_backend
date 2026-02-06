package org.patifiner.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import org.patifiner.auth.exceptions.AuthException
import org.patifiner.base.PtfJwtConfig
import java.util.Date

const val JWT_AUTH = "auth-jwt"
private const val USER_ID_FIELD_NAME = "id"
private const val TOKEN_TYPE_FIELD_NAME = "type"

private val PtfJwtConfig.algorithm
    get(): Algorithm = Algorithm.HMAC256(secret)

enum class TokenType { ACCESS, REFRESH }

fun ApplicationCall.getCurrentUserId(): Long =
    principal<JWTPrincipal>()?.getClaim(USER_ID_FIELD_NAME, Long::class) ?: throw AuthException.InvalidTokenException()

fun generateToken(jwtConfig: PtfJwtConfig, userId: Long, type: TokenType = TokenType.ACCESS): String {
    val expiration = if (type == TokenType.ACCESS) jwtConfig.accessTokenExpirationMs else jwtConfig.refreshTokenExpirationMs
    return JWT.create()
        .withAudience(jwtConfig.audience)
        .withIssuer(jwtConfig.issuer)
        .withClaim(USER_ID_FIELD_NAME, userId)
        .withClaim(TOKEN_TYPE_FIELD_NAME, type.name)
        .withExpiresAt(Date(System.currentTimeMillis() + expiration))
        .sign(jwtConfig.algorithm)
}

fun verifyRefreshToken(jwtConfig: PtfJwtConfig, token: String): Long {
    return try {
        val verifier = JWT.require(jwtConfig.algorithm)
            .withAudience(jwtConfig.audience)
            .withIssuer(jwtConfig.issuer)
            .withClaim(TOKEN_TYPE_FIELD_NAME, TokenType.REFRESH.name)
            .build()
        val decoded = verifier.verify(token)
        decoded.getClaim(USER_ID_FIELD_NAME).asLong() ?: throw AuthException.InvalidRefreshTokenException()
    } catch (_: Exception) {
        throw AuthException.InvalidRefreshTokenException()
    }
}

fun Application.installPtfAuth(config: PtfJwtConfig) {
    install(Authentication) {
        jwt(JWT_AUTH) {
            verifier(
                JWT.require(config.algorithm)
                    .withAudience(config.audience)
                    .withIssuer(config.issuer)
                    .withClaim(TOKEN_TYPE_FIELD_NAME, TokenType.ACCESS.name)
                    .build()
            )
            realm = config.realm

            validate { credential ->
                if (credential.payload.getClaim(USER_ID_FIELD_NAME).asLong() == null) null
                else if (credential.payload.audience.contains(config.audience)) JWTPrincipal(
                    credential.payload
                )
                else null
            }

            challenge { _, _ -> throw AuthException.InvalidTokenException() }
        }
    }
}
