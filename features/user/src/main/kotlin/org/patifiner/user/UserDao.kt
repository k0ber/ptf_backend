package org.patifiner.user

import org.patifiner.database.tables.CityEntity
import org.patifiner.database.tables.UserEntity
import org.patifiner.database.tables.UserTable
import org.patifiner.user.api.CreateUserRequest
import org.patifiner.user.api.UpdateUserRequest
import org.patifiner.user.api.UserException
import org.patifiner.user.api.UserException.UserNotFoundByIdException

interface UserDao {
    fun existsByEmail(email: String): Boolean
    fun create(userInfo: CreateUserRequest, hashedPassword: String): UserDto

    @Throws(UserNotFoundByIdException::class)
    fun getById(id: Long): UserEntity

    fun findByEmail(email: String): UserEntity?
    fun getUsersByIds(ids: List<Long>): List<UserDto>
    fun updateUserMedia(userId: Long, photos: List<String>, avatarUrl: String?): UserDto
    fun updateAvatarUrl(userId: Long, avatarUrl: String?): UserDto
    fun updatePhotos(userId: Long, photos: List<String>): UserDto
    fun updateProfile(userId: Long, request: UpdateUserRequest): UserDto
}

internal class ExposedUserDao : UserDao {

    override fun existsByEmail(email: String): Boolean = UserEntity.find { UserTable.email eq email }.any()

    override fun create(userInfo: CreateUserRequest, hashedPassword: String): UserDto =
        UserEntity.new { fromCreateRequest(userInfo, hashedPassword) }.toDto()

    override fun getById(id: Long): UserEntity =
        UserEntity.find { UserTable.id eq id }.singleOrNull() ?: throw UserNotFoundByIdException(id)

    override fun findByEmail(email: String): UserEntity? =
        UserEntity.find { UserTable.email eq email }.singleOrNull()

    override fun getUsersByIds(ids: List<Long>): List<UserDto> {
        val users = UserEntity.find { UserTable.id inList ids }.associateBy { it.id.value }
        return ids.mapNotNull { users[it]?.toDto() }
    }

    override fun updateAvatarUrl(userId: Long, avatarUrl: String?): UserDto {
        val user = UserEntity.findById(userId) ?: throw UserNotFoundByIdException(userId)
        user.avatarUrl = avatarUrl
        return user.toDto()
    }

    override fun updatePhotos(userId: Long, photos: List<String>): UserDto {
        val user = UserEntity.findById(userId) ?: throw UserNotFoundByIdException(userId)
        user.photos = photos
        return user.toDto()
    }

    override fun updateProfile(userId: Long, request: UpdateUserRequest): UserDto {
        val user = UserEntity.findById(userId) ?: throw UserNotFoundByIdException(userId)
        val city = request.cityId?.let { CityEntity.findById(it) ?: throw UserException.CityNotFoundException(it) }
        user.fromUpdateRequest(request, city)
        return user.toDto()
    }

    override fun updateUserMedia(userId: Long, photos: List<String>, avatarUrl: String?): UserDto {
        val user = UserEntity.findById(userId) ?: throw UserNotFoundByIdException(userId)
        user.photos = photos
        user.avatarUrl = avatarUrl
        return user.toDto()
    }
}
