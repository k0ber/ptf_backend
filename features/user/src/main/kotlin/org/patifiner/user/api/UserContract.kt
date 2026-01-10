package org.patifiner.user.api

import org.patifiner.user.UserInfoDto
import java.time.LocalDate

data class TokenRequest(val email: String, val password: String)

data class TokenResponse(
    val token: String
)

data class CreateUserRequest(
    val name: String,
    val surname: String,
    val birthDate: LocalDate?,
    val email: String,
    val password: String
)

data class UserCreatedResponse(
    val userInfo: UserInfoDto,
    val token: String
)

data class UpdateUserRequest(
    val avatarUrl: String? = null,
    val photos: List<String>? = null
)

data class SetMainPhotoRequest(val url: String)

data class DeletePhotoRequest(val url: String)
