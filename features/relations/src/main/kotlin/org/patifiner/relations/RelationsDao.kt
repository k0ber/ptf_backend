package org.patifiner.relations

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.patifiner.database.enums.RelationStatus
import org.patifiner.database.tables.UserEntity
import org.patifiner.database.tables.UserRelationEntity
import org.patifiner.database.tables.UserRelationsTable

interface RelationsDao {
    fun getRelation(fromUserId: Long, toUserId: Long): UserRelationEntity?
    fun createRelation(fromUserId: Long, toUserId: Long, status: RelationStatus): UserRelationEntity
    fun updateRelationStatus(relationId: Long, status: RelationStatus): UserRelationEntity
    fun findUserRelations(userId: Long): List<UserRelationEntity>
    fun getMutedUserIds(userId: Long): Set<Long>
}

internal class ExposedRelationsDao : RelationsDao {

    override fun getRelation(fromUserId: Long, toUserId: Long): UserRelationEntity? =
        UserRelationEntity.find {
            ((UserRelationsTable.fromUser eq fromUserId) and (UserRelationsTable.toUser eq toUserId)) or
                    ((UserRelationsTable.fromUser eq toUserId) and (UserRelationsTable.toUser eq fromUserId))
        }.singleOrNull()

    override fun createRelation(fromUserId: Long, toUserId: Long, status: RelationStatus): UserRelationEntity =
        UserRelationEntity.new {
            this.fromUser = UserEntity[fromUserId]
            this.toUser = UserEntity[toUserId]
            this.status = status
        }

    override fun updateRelationStatus(relationId: Long, status: RelationStatus): UserRelationEntity =
        UserRelationEntity[relationId].apply {
            this.status = status
            this.updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }

    override fun findUserRelations(userId: Long): List<UserRelationEntity> =
        UserRelationEntity.find {
            (UserRelationsTable.fromUser eq userId) or (UserRelationsTable.toUser eq userId)
        }.toList()

    override fun getMutedUserIds(userId: Long): Set<Long> =
        UserRelationEntity.find {
            ((UserRelationsTable.fromUser eq userId) or (UserRelationsTable.toUser eq userId)) and
                    (UserRelationsTable.status eq RelationStatus.BLOCKED)
        }.map {
            if (it.fromUser.id.value == userId) it.toUser.id.value
            else it.fromUser.id.value
        }.toSet()
}
