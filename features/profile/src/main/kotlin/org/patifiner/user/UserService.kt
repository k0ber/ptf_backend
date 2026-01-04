package org.patifiner.user

import org.mindrot.jbcrypt.BCrypt
import org.patifiner.auth.JwtConfig
import org.patifiner.auth.generateToken
import org.patifiner.user.api.CreateUserRequest
import org.patifiner.user.api.UserCreatedResponse
import org.patifiner.user.data.UserDao
import org.patifiner.user.api.UserException.EmailAlreadyTakenException
import org.patifiner.user.api.UserException.InvalidCredentialsException
import org.patifiner.user.api.UserException.UserNotFoundByEmailException

internal class UserService(
    private val userDao: UserDao,
    private val jwtInfo: JwtConfig
) {

    suspend fun createUser(userInfo: CreateUserRequest): UserCreatedResponse {
        if (userDao.existsByEmail(userInfo.email)) {
            throw EmailAlreadyTakenException(userInfo.email)
        }

        val hashedPwd = BCrypt.hashpw(userInfo.password, BCrypt.gensalt())
        val createdUser = userDao.create(userInfo, hashedPwd)

        return UserCreatedResponse(
            userInfo = createdUser,
            token = generateToken(jwtInfo, createdUser.id)
        )
    }

    suspend fun requestToken(email: String, password: String): String {
        val userEntity = userDao.findByEmail(email) ?: throw UserNotFoundByEmailException(email)

        if (!BCrypt.checkpw(password, userEntity.password)) {
            throw InvalidCredentialsException()
        }

        return generateToken(jwtInfo, userEntity.id.value)
    }

    suspend fun getUserInfo(userId: Long): UserInfoDto = userDao.findById(userId).toDto()

    suspend fun updateAvatarUrl(userId: Long, avatarUrl: String?): UserInfoDto {
        // Здесь можно добавить валидацию URL, если нужно.
        return userDao.updateAvatarUrl(userId, avatarUrl)
    }
}
