package org.patifiner.user

import org.mindrot.jbcrypt.BCrypt
import org.patifiner.auth.JwtConfig
import org.patifiner.auth.generateToken
import org.patifiner.user.api.CreateUserRequest
import org.patifiner.user.api.UserCreatedResponse
import org.patifiner.user.api.UserException
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
            token = generateToken(jwtConfig, createdUser.id)
        )
    }

    suspend fun requestToken(email: String, password: String): String {
        val userEntity = userDao.findByEmail(email) ?: throw UserNotFoundByEmailException(email)

        if (!BCrypt.checkpw(password, userEntity.password)) {
            throw InvalidCredentialsException()
        }

        return generateToken(jwtConfig, userEntity.id.value)
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
        val user = userDao.getById(userId)?: throw UserNotFoundByIdException(userId)
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
