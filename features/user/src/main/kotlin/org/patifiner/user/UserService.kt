package org.patifiner.user

import org.mindrot.jbcrypt.BCrypt
import org.patifiner.auth.JwtConfig
import org.patifiner.auth.TokenType
import org.patifiner.auth.generateToken
import org.patifiner.auth.verifyRefreshToken
import org.patifiner.user.api.*
import org.patifiner.user.api.UserException.*

private const val USER_PHOTO_LIMIT = 10

internal class UserService(
    private val userDao: UserDao,
    private val jwtConfig: JwtConfig,
) {

    suspend fun createUser(userInfo: CreateUserRequest): UserCreatedResponse {
        if (userDao.existsByEmail(userInfo.email)) {
            throw EmailAlreadyTakenException(userInfo.email)
        }

        val hashedPwd = BCrypt.hashpw(userInfo.password, BCrypt.gensalt())
        val createdUser = userDao.create(userInfo, hashedPwd)

        return UserCreatedResponse(
            userInfo = createdUser,
            token = generateTokenPair(createdUser.id)
        )
    }

    suspend fun requestToken(email: String, password: String): TokenResponse {
        val userEntity = userDao.findByEmail(email) ?: throw UserNotFoundByEmailException(email)

        if (!BCrypt.checkpw(password, userEntity.password)) {
            throw InvalidCredentialsException()
        }

        return generateTokenPair(userEntity.id.value)
    }

    fun refreshToken(refreshToken: String): TokenResponse {
        val userId = verifyRefreshToken(jwtConfig, refreshToken)
        return generateTokenPair(userId)
    }

    private fun generateTokenPair(userId: Long): TokenResponse {
        return TokenResponse(
            accessToken = generateToken(jwtConfig, userId, TokenType.ACCESS),
            refreshToken = generateToken(jwtConfig, userId, TokenType.REFRESH)
        )
    }

    suspend fun getUserInfo(userId: Long): UserDto = userDao.getById(userId).toDto()

    suspend fun updateProfile(userId: Long, request: UpdateUserRequest): UserDto =
        userDao.updateProfile(userId, request)

    suspend fun updateAvatar(userId: Long, avatarUrl: String?): UserDto =
        userDao.updateAvatarUrl(userId, avatarUrl)

    suspend fun addPhoto(userId: Long, photoUrl: String): UserDto {
        val user = userDao.getById(userId)
        if (user.photos.size >= USER_PHOTO_LIMIT) {
            throw PhotoLimitReachedException()
        }
        return userDao.updatePhotos(userId, user.photos + photoUrl)
    }

    suspend fun removePhoto(userId: Long, photoToRemove: String): UserDto {
        val user = userDao.getById(userId)
        val updatedPhotos = user.photos - photoToRemove
        val newAvatarUrl = if (user.avatarUrl == photoToRemove) null else user.avatarUrl
        return userDao.updateUserMedia(userId, updatedPhotos, newAvatarUrl)
    }
}
