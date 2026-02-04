package org.patifiner.user

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.mindrot.jbcrypt.BCrypt
import org.patifiner.auth.TokenType
import org.patifiner.auth.generateToken
import org.patifiner.auth.verifyRefreshToken
import org.patifiner.base.PtfJwtConfig
import org.patifiner.user.api.CreateUserRequest
import org.patifiner.user.api.TokenResponse
import org.patifiner.user.api.UpdateUserRequest
import org.patifiner.user.api.UserCreatedResponse
import org.patifiner.user.api.UserException.EmailAlreadyTakenException
import org.patifiner.user.api.UserException.InvalidCredentialsException
import org.patifiner.user.api.UserException.PhotoLimitReachedException
import org.patifiner.user.api.UserException.UserNotFoundByEmailException

private const val USER_PHOTO_LIMIT = 10

internal class UserService(
    private val userDao: UserDao,
    private val jwtConfig: PtfJwtConfig,
) {

    suspend fun createUser(userInfo: CreateUserRequest): UserCreatedResponse = newSuspendedTransaction(Dispatchers.IO) {
        if (userDao.existsByEmail(userInfo.email)) {
            throw EmailAlreadyTakenException(userInfo.email)
        }

        val hashedPwd = BCrypt.hashpw(userInfo.password, BCrypt.gensalt())
        val createdUser = userDao.create(userInfo, hashedPwd)

        UserCreatedResponse(
            userInfo = createdUser,
            token = generateTokenPair(createdUser.id)
        )
    }

    suspend fun requestToken(email: String, password: String): TokenResponse = newSuspendedTransaction(Dispatchers.IO) {
        val userEntity = userDao.findByEmail(email) ?: throw UserNotFoundByEmailException(email)

        if (!BCrypt.checkpw(password, userEntity.password)) {
            throw InvalidCredentialsException()
        }

        generateTokenPair(userEntity.id.value)
    }

    suspend fun getUserInfo(userId: Long): UserDto = newSuspendedTransaction(Dispatchers.IO) {
        userDao.getById(userId).toDto()
    }

    suspend fun updateProfile(userId: Long, request: UpdateUserRequest): UserDto = newSuspendedTransaction(Dispatchers.IO) {
        userDao.updateProfile(userId, request)
    }

    suspend fun updateAvatar(userId: Long, avatarUrl: String?): UserDto = newSuspendedTransaction(Dispatchers.IO) {
        userDao.updateAvatarUrl(userId, avatarUrl)
    }

    suspend fun addPhoto(userId: Long, photoUrl: String): UserDto = newSuspendedTransaction(Dispatchers.IO) {
        val user = userDao.getById(userId)
        if (user.photos.size >= USER_PHOTO_LIMIT) {
            throw PhotoLimitReachedException()
        }
        userDao.updatePhotos(userId, user.photos + photoUrl)
    }

    suspend fun removePhoto(userId: Long, photoToRemove: String): UserDto = newSuspendedTransaction(Dispatchers.IO) {
        val user = userDao.getById(userId)
        val updatedPhotos = user.photos - photoToRemove
        val newAvatarUrl = if (user.avatarUrl == photoToRemove) null else user.avatarUrl
        userDao.updateUserMedia(userId, updatedPhotos, newAvatarUrl)
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
}
