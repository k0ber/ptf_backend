package org.patifiner.user.api


sealed class UserException(override val message: String) : RuntimeException(message) {
    class UserNotFoundByIdException(id: Long) : UserException("User with id $id not found")
    class InvalidCredentialsException : UserException("Invalid email or password")
    class EmailAlreadyTakenException(email: String) : UserException("Email already taken: $email")
    class UserNotFoundByEmailException(val email: String) : UserException("User not found: $email")
}
