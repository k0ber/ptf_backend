package org.patifiner.relations.api

import org.patifiner.database.enums.RelationStatus

data class UpdateRelationRequest(
    val targetUserId: Long,
    val status: RelationStatus
)
