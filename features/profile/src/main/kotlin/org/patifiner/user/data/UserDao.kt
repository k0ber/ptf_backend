package org.patifiner.user.data

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.patifiner.user.UserInfoDto
import org.patifiner.user.api.UserException.UserNotFoundByIdException
import org.patifiner.topics.data.UserTopicsTable
import org.patifiner.user.api.CreateUserRequest

interface UserDao {
    suspend fun existsByEmail(email: String): Boolean
    suspend fun create(userInfo: CreateUserRequest, hashedPassword: String): UserInfoDto
    suspend fun findById(id: Long): UserEntity
    suspend fun findByEmail(email: String): UserEntity?
    suspend fun findUsersByAnyTopics(
        topicIds: Collection<Long>,
        excludeUserId: Long,
        limit: Int,
        offset: Int
    ): Set<UserInfoDto>

    suspend fun updateAvatarUrl(userId: Long, avatarUrl: String?): UserInfoDto
}

internal class ExposedUserDao : UserDao {

    override suspend fun existsByEmail(email: String): Boolean =
        newSuspendedTransaction(Dispatchers.IO) {
            UserEntity.find { UsersTable.email eq email }.any()
        }

    override suspend fun create(userInfo: CreateUserRequest, hashedPassword: String): UserInfoDto =
        newSuspendedTransaction(Dispatchers.IO) {
            val user = UserEntity.new { fromDto(userInfo, hashedPassword) }
            user.toDto()
        }

    // todo: should throw inside or on the call site and nullable?
    override suspend fun findById(id: Long): UserEntity =
        newSuspendedTransaction(Dispatchers.IO) {
            UserEntity.find { UsersTable.id eq id }.singleOrNull()
                ?: throw UserNotFoundByIdException(id)
        }

    override suspend fun findByEmail(email: String): UserEntity? =
        newSuspendedTransaction(Dispatchers.IO) {
            UserEntity.find { UsersTable.email eq email }.singleOrNull()
        }

    override suspend fun findUsersByAnyTopics(
        topicIds: Collection<Long>,
        excludeUserId: Long,
        limit: Int,
        offset: Int
    ): Set<UserInfoDto> = newSuspendedTransaction(Dispatchers.IO) {
        if (topicIds.isEmpty()) return@newSuspendedTransaction emptySet()

        // 1) Берём нужные user_id с пересечением топиков (без текущего пользователя)
        val userIds: List<Long> = UserTopicsTable
            .slice(UserTopicsTable.user)
            .select { (UserTopicsTable.topic inList topicIds) and (UserTopicsTable.user neq excludeUserId) }
            .groupBy(UserTopicsTable.user)
            .limit(n = limit, offset = offset.toLong())
            .map { row -> row[UserTopicsTable.user].value }

        if (userIds.isEmpty()) return@newSuspendedTransaction emptySet()

        // 2) Загружаем пользователей одним запросом и мапим по id
        val usersById: Map<Long, UserEntity> = UserEntity.find { UsersTable.id inList userIds }
            .associateBy { it.id.value }

        // 3) Соблюдаем исходный порядок (LIMIT/OFFSET) и мапим в DTO
        userIds.mapNotNull { usersById[it]?.toDto() }.toSet()
    }

    override suspend fun updateAvatarUrl(userId: Long, avatarUrl: String?): UserInfoDto =
        newSuspendedTransaction(Dispatchers.IO) {
            val user = UserEntity.findById(userId) ?: throw UserNotFoundByIdException(userId)
            user.avatarUrl = avatarUrl
            user.toDto()
        }
}
