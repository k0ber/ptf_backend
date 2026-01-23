package org.patifiner.relations

import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.patifiner.database.enums.RelationStatus
import org.patifiner.database.tables.UserEntity
import org.patifiner.database.tables.UserRelationEntity
import org.patifiner.database.tables.UserRelationsTable

interface RelationsDao {
    suspend fun getRelation(fromUserId: Long, toUserId: Long): UserRelationEntity?
    suspend fun createRelation(fromUserId: Long, toUserId: Long, status: RelationStatus): UserRelationEntity
    suspend fun updateRelationStatus(relationId: Long, status: RelationStatus): UserRelationEntity
    suspend fun findUserRelations(userId: Long): List<UserRelationEntity>
    suspend fun getMutedUserIds(userId: Long): Set<Long>
}

internal class ExposedRelationsDao : RelationsDao {

    override suspend fun getRelation(fromUserId: Long, toUserId: Long): UserRelationEntity? =
        newSuspendedTransaction(Dispatchers.IO) {
            UserRelationEntity.find {
                ((UserRelationsTable.fromUser eq fromUserId) and (UserRelationsTable.toUser eq toUserId)) or
                        ((UserRelationsTable.fromUser eq toUserId) and (UserRelationsTable.toUser eq fromUserId))
            }.singleOrNull()
        }

    override suspend fun createRelation(fromUserId: Long, toUserId: Long, status: RelationStatus): UserRelationEntity =
        newSuspendedTransaction(Dispatchers.IO) {
            UserRelationEntity.new {
                this.fromUser = UserEntity[fromUserId]
                this.toUser = UserEntity[toUserId]
                this.status = status
            }
        }

    override suspend fun updateRelationStatus(relationId: Long, status: RelationStatus): UserRelationEntity =
        newSuspendedTransaction(Dispatchers.IO) {
            UserRelationEntity[relationId].apply {
                this.status = status
                this.updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            }
        }

    override suspend fun findUserRelations(userId: Long): List<UserRelationEntity> =
        newSuspendedTransaction(Dispatchers.IO) {
            UserRelationEntity.find {
                (UserRelationsTable.fromUser eq userId) or (UserRelationsTable.toUser eq userId)
            }.toList()
        }

    override suspend fun getMutedUserIds(userId: Long): Set<Long> =
        newSuspendedTransaction(Dispatchers.IO) {
            UserRelationEntity.find {
                ((UserRelationsTable.fromUser eq userId) or (UserRelationsTable.toUser eq userId)) and
                        (UserRelationsTable.status eq RelationStatus.BLOCKED)
            }.map {
                if (it.fromUser.id.value == userId) it.toUser.id.value
                else it.fromUser.id.value
            }.toSet()
        }
}
