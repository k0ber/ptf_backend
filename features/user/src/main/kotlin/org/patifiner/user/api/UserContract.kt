package org.patifiner.user.api

import org.patifiner.user.UserInfoDto

data class TokenResponse(val accessToken: String, val refreshToken: String)

data class RefreshTokenRequest(val refreshToken: String)

data class TokenRequest(val email: String, val password: String)

data class CreateUserRequest(val name: String, val email: String, val password: String)

data class UserCreatedResponse(val userInfo: UserInfoDto, val token: TokenResponse)

data class UpdateUserRequest(val avatarUrl: String?)

data class DeletePhotoRequest(val url: String)

data class SetMainPhotoRequest(val url: String)

data class UpdateCityRequest(val cityId: Long?)
