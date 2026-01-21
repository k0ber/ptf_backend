package org.patifiner.user.api

import org.patifiner.database.enums.Gender
import org.patifiner.database.enums.UserLanguage
import org.patifiner.user.UserDto

data class TokenResponse(val accessToken: String, val refreshToken: String)

data class RefreshTokenRequest(val refreshToken: String)

data class TokenRequest(val email: String, val password: String)

data class CreateUserRequest(val name: String, val email: String, val password: String)

data class UserCreatedResponse(val userInfo: UserDto, val token: TokenResponse)

data class UpdateUserRequest(
    val name: String,
    val birthDate: String? = null,
    val gender: Gender = Gender.NOT_SPECIFIED,
    val cityId: Long? = null,
    val languages: List<UserLanguage> = emptyList()
)

data class DeletePhotoRequest(val url: String)

data class SetMainPhotoRequest(val url: String)
