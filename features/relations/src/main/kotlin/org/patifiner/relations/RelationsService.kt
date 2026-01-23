package org.patifiner.relations

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.patifiner.database.enums.RelationStatus
import org.patifiner.database.tables.UserEntity
import org.patifiner.relations.api.RelationsException.ActionNotAllowedException
import org.patifiner.relations.api.RelationsException.AlreadyExistsException
import org.patifiner.relations.api.RelationsException.RelationNotFoundException

internal class RelationsService(
    private val relationsDao: RelationsDao
) {
    suspend fun getMyRelations(userId: Long): List<UserRelationDto> =
        relationsDao.findUserRelations(userId).map { it.toDto(userId) }

    suspend fun blockUser(currentUserId: Long, targetUserId: Long): UserRelationDto {
        if (currentUserId == targetUserId) throw ActionNotAllowedException()

        return newSuspendedTransaction(Dispatchers.IO) {
            val existing = relationsDao.getRelation(currentUserId, targetUserId)

            val relation = if (existing != null) {
                existing.apply {
                    this.fromUser = UserEntity[currentUserId]
                    this.toUser = UserEntity[targetUserId]
                    this.status = RelationStatus.BLOCKED
                }
                relationsDao.updateRelationStatus(existing.id.value, RelationStatus.BLOCKED)
            } else {
                relationsDao.createRelation(currentUserId, targetUserId, RelationStatus.BLOCKED)
            }

            relation.toDto(currentUserId)
        }
    }

    suspend fun sendInvite(fromUserId: Long, toUserId: Long): UserRelationDto {
        if (fromUserId == toUserId) throw ActionNotAllowedException()
        if (relationsDao.getRelation(fromUserId, toUserId) != null) throw AlreadyExistsException()

        val newRelation = relationsDao.createRelation(fromUserId, toUserId, RelationStatus.PENDING)
        return newRelation.toDto(fromUserId)
    }

    suspend fun acceptInvite(currentUserId: Long, targetUserId: Long): UserRelationDto {
        val relation = relationsDao.getRelation(targetUserId, currentUserId) ?: throw RelationNotFoundException()

        if (relation.status != RelationStatus.PENDING) throw ActionNotAllowedException()

        val updated = relationsDao.updateRelationStatus(relation.id.value, RelationStatus.ACCEPTED)
        return updated.toDto(currentUserId)
    }
}
