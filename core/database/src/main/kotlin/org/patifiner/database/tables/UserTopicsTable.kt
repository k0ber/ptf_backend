package org.patifiner.database.tables

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.patifiner.database.enums.TopicLevel

object UserTopicsTable : LongIdTable("user_topics") {
    val user = reference("user_id", UserTable)
    val topic = reference("topic_id", TopicsTable)
    val level = enumerationByName("level", 20, TopicLevel::class)
        .default(TopicLevel.NONE)
    val description = varchar("description", 250).nullable()

    init {
        uniqueIndex("ux_user_topic", user, topic)
    }
}

class UserTopicEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserTopicEntity>(UserTopicsTable)

    var user by UserEntity referencedOn UserTopicsTable.user
    var topic by TopicEntity referencedOn UserTopicsTable.topic
    var level by UserTopicsTable.level
    var description by UserTopicsTable.description
}
