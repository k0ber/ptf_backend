package org.patifiner.user.api

import io.ktor.http.HttpStatusCode
import org.patifiner.base.PatifinerException


sealed class UserException(message: String, code: String, statusCode: HttpStatusCode) : PatifinerException(message, code, statusCode) {
    class UserNotFoundByIdException(id: Long) : UserException("User with id $id not found", "USER_NOT_FOUND", HttpStatusCode.NotFound)
    class InvalidCredentialsException : UserException("Invalid email or password", "AUTH_INVALID_CREDENTIALS", HttpStatusCode.Unauthorized)
    class EmailAlreadyTakenException(email: String) : UserException("Email already taken: $email", "USER_EMAIL_TAKEN", HttpStatusCode.Conflict)
    class UserNotFoundByEmailException(email: String) : UserException("User not found: $email", "USER_NOT_FOUND", HttpStatusCode.NotFound)
    class FileIsMissing() : UserException("File is missing", "FILE_MISSING", HttpStatusCode.BadRequest)
    class PhotoLimitReachedException() : UserException("Photos limit reached", "PHOTO_LIMIT_REACHED", HttpStatusCode.BadRequest)
    class CityNotFoundException(id: Long) : UserException("City with id $id not found", "CITY_NOT_FOUND", HttpStatusCode.NotFound)
}
