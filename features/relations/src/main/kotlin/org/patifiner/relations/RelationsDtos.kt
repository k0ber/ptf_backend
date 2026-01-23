package org.patifiner.relations

import org.patifiner.database.enums.RelationStatus
import org.patifiner.database.tables.UserRelationEntity
import org.patifiner.user.UserDto
import org.patifiner.user.toDto

data class UserRelationDto(
    val id: Long,
    val relatedUser: UserDto,
    val status: RelationStatus,
    val isIncoming: Boolean
)

fun UserRelationEntity.toDto(currentUserId: Long): UserRelationDto {
    val isIncoming = this.toUser.id.value == currentUserId
    val relatedUser = if (isIncoming) this.fromUser else this.toUser

    return UserRelationDto(
        id = this.id.value,
        relatedUser = relatedUser.toDto(),
        status = this.status,
        isIncoming = isIncoming
    )
}
