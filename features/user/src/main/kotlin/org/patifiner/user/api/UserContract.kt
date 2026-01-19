package org.patifiner.user.api

import kotlinx.serialization.Serializable
import org.patifiner.user.UserInfoDto

@Serializable data class TokenResponse(val accessToken: String, val refreshToken: String)
@Serializable data class RefreshTokenRequest(val refreshToken: String)
@Serializable data class TokenRequest(val email: String, val password: String)
@Serializable data class CreateUserRequest(val name: String, val email: String, val password: String)
@Serializable data class UserCreatedResponse(val userInfo: UserInfoDto, val token: TokenResponse)
@Serializable data class UpdateUserRequest(val avatarUrl: String?)
@Serializable data class DeletePhotoRequest(val url: String)
@Serializable data class SetMainPhotoRequest(val url: String)
@Serializable data class UpdateCityRequest(val cityId: Long?)
