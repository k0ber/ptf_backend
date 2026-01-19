package org.patifiner.user

import org.mindrot.jbcrypt.BCrypt
import org.patifiner.auth.JwtConfig
import org.patifiner.auth.TokenType
import org.patifiner.auth.generateToken
import org.patifiner.auth.verifyRefreshToken
import org.patifiner.user.api.*
import org.patifiner.user.api.UserException.EmailAlreadyTakenException
import org.patifiner.user.api.UserException.InvalidCredentialsException
import org.patifiner.user.api.UserException.UserNotFoundByEmailException
import org.patifiner.user.api.UserException.UserNotFoundByIdException

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

    suspend fun getUserInfo(userId: Long): UserInfoDto = userDao.getById(userId)?.toDto()?: throw UserNotFoundByIdException(userId)

    suspend fun updateAvatarUrl(userId: Long, avatarUrl: String?): UserInfoDto {
        userDao.getById(userId)
        return userDao.updateAvatarUrl(userId, avatarUrl)
    }

    suspend fun addPhoto(userId: Long, photoUrl: String): UserInfoDto {
        val user = userDao.getById(userId)
        val currentPhotos = user.photosList.toMutableList()

        if (currentPhotos.size >= USER_PHOTO_LIMIT) {
            throw UserException.PhotoLimitReachedException()
        }

        currentPhotos.add(photoUrl)
        return userDao.updatePhotos(userId, currentPhotos)
    }

    suspend fun removePhoto(userId: Long, photoUrl: String): String {
        val user = userDao.getById(userId)
        val currentPhotos = user.photosList.toMutableList()

        if (currentPhotos.remove(photoUrl)) {
            userDao.updatePhotos(userId, currentPhotos)
        }

        if (user.avatarUrl == photoUrl) {
            userDao.updateAvatarUrl(userId, null)
        }

        return photoUrl
    }

    suspend fun setMainPhoto(userId: Long, photoUrl: String): UserInfoDto = userDao.updateAvatarUrl(userId, photoUrl)

    suspend fun updateCity(userId: Long, cityId: Long?): UserInfoDto = userDao.updateCity(userId, cityId)

}
