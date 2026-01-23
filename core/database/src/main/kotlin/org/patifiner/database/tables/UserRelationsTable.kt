package org.patifiner.database.tables

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.patifiner.database.enums.RelationStatus

object UserRelationsTable : LongIdTable("user_relations") {
    val fromUser = reference("from_user_id", UserTable)
    val toUser = reference("to_user_id", UserTable)
    val status = enumerationByName("status", 20, RelationStatus::class).default(RelationStatus.PENDING)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    init {
        uniqueIndex(fromUser, toUser) // Уникальный индекс, чтобы нельзя было создать дублирующуюся связь
    }
}

class UserRelationEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserRelationEntity>(UserRelationsTable)

    var fromUser by UserEntity referencedOn UserRelationsTable.fromUser
    var toUser by UserEntity referencedOn UserRelationsTable.toUser
    var status by UserRelationsTable.status
    var createdAt by UserRelationsTable.createdAt
    var updatedAt by UserRelationsTable.updatedAt
}
