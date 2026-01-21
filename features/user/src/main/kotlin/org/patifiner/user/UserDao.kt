package org.patifiner.user

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.patifiner.base.PagedRequest
import org.patifiner.base.calculateOffset
import org.patifiner.database.tables.CityEntity
import org.patifiner.database.tables.UserEntity
import org.patifiner.database.tables.UserTable
import org.patifiner.database.tables.UserTopicsTable
import org.patifiner.user.api.CreateUserRequest
import org.patifiner.user.api.UpdateUserRequest
import org.patifiner.user.api.UserException
import org.patifiner.user.api.UserException.UserNotFoundByIdException

interface UserDao {
    suspend fun existsByEmail(email: String): Boolean
    suspend fun create(userInfo: CreateUserRequest, hashedPassword: String): UserDto

    @Throws(UserNotFoundByIdException::class)
    suspend fun getById(id: Long): UserEntity

    suspend fun findByEmail(email: String): UserEntity?
    suspend fun getUsersByIds(ids: List<Long>): List<UserDto>
    suspend fun updateUserMedia(userId: Long, photos: List<String>, avatarUrl: String?): UserDto
    suspend fun updateAvatarUrl(userId: Long, avatarUrl: String?): UserDto
    suspend fun updatePhotos(userId: Long, photos: List<String>): UserDto
    suspend fun updateProfile(userId: Long, request: UpdateUserRequest): UserDto
}

internal class ExposedUserDao : UserDao {

    override suspend fun existsByEmail(email: String): Boolean =
        newSuspendedTransaction(Dispatchers.IO) {
            UserEntity.find { UserTable.email eq email }.any()
        }

    override suspend fun create(userInfo: CreateUserRequest, hashedPassword: String): UserDto =
        newSuspendedTransaction(Dispatchers.IO) {
            val user = UserEntity.new { fromCreateRequest(userInfo, hashedPassword) }
            user.toDto()
        }

    /**
     *  get метод в дао возвращает не налабл и может кинуть ошибку UserNotFoundByIdException если по айди ничего не найдётся
     */
    override suspend fun getById(id: Long): UserEntity =
        newSuspendedTransaction(Dispatchers.IO) {
            UserEntity.find { UserTable.id eq id }.singleOrNull() ?: throw UserNotFoundByIdException(id)
        }

    /**
     *  find в методе в дао указывает на налабл в возвращаемом типе
     */
    override suspend fun findByEmail(email: String): UserEntity? =
        newSuspendedTransaction(Dispatchers.IO) {
            UserEntity.find { UserTable.email eq email }.singleOrNull()
        }

    override suspend fun getUsersByIds(ids: List<Long>): List<UserDto> =
        newSuspendedTransaction(Dispatchers.IO) {
            val users = UserEntity.find { UserTable.id inList ids }
                .associateBy { it.id.value }

            // Сохраняем порядок, который пришел из поиска (важно для пагинации)
            ids.mapNotNull { users[it]?.toDto() }
        }

    override suspend fun updateAvatarUrl(userId: Long, avatarUrl: String?): UserDto =
        newSuspendedTransaction(Dispatchers.IO) {
            val user = UserEntity.findById(userId) ?: throw UserNotFoundByIdException(userId)
            user.avatarUrl = avatarUrl
            user.toDto()
        }

    override suspend fun updatePhotos(userId: Long, photos: List<String>): UserDto =
        newSuspendedTransaction(Dispatchers.IO) {
            val user = UserEntity.findById(userId) ?: throw UserNotFoundByIdException(userId)
            user.photos = photos
            user.toDto()
        }

    override suspend fun updateProfile(userId: Long, request: UpdateUserRequest): UserDto =
        newSuspendedTransaction(Dispatchers.IO) {
            val user = UserEntity.findById(userId) ?: throw UserNotFoundByIdException(userId)

            // Если ID города пришел - ищем город, если нет - получаем null
            // Если ID был передан, но город не найден - кидаем ошибку
            val city = request.cityId?.let {
                CityEntity.findById(it) ?: throw UserException.CityNotFoundException(it)
            }

            user.fromUpdateRequest(request, city)
            user.toDto()
        }

    override suspend fun updateUserMedia(userId: Long, photos: List<String>, avatarUrl: String?): UserDto =
        newSuspendedTransaction(Dispatchers.IO) {
            val user = UserEntity.findById(userId) ?: throw UserNotFoundByIdException(userId)
            user.photos = photos
            user.avatarUrl = avatarUrl
            user.toDto()
        }
}
