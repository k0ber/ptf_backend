package org.patifiner.auth.exceptions

// todo: refactor exceptions
sealed class AuthException(message: String) : RuntimeException(message) {
    class InvalidTokenException() : AuthException("Invalid or expired token")
}
