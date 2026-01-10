package org.patifiner.auth.exceptions

import io.ktor.http.HttpStatusCode
import org.patifiner.base.PatifinerException

sealed class AuthException(message: String, code: String) : PatifinerException(message, code, HttpStatusCode.Unauthorized) {
    class InvalidTokenException : AuthException("Invalid or expired token", "AUTH_INVALID_TOKEN")
}
