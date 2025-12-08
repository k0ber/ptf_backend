package org.patifiner.topics.data

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.patifiner.topics.TopicDto
import org.patifiner.topics.TopicLevel
import org.patifiner.topics.UserTopicDto
import org.patifiner.user.data.UserEntity
import org.patifiner.user.data.UsersTable

object TopicsTable : LongIdTable("topics") {
    val name = varchar("name", 255)
    val slug = varchar("slug", 255).uniqueIndex()
    val description = text("description").nullable()
    val tags = text("tags").nullable()
    val icon = varchar("icon", 32).nullable()
    val parent = reference("parent_id", TopicsTable).nullable()
    val locale = varchar("locale", 8).default("en")
}

class TopicEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<TopicEntity>(TopicsTable)

    var name by TopicsTable.name
    var slug by TopicsTable.slug
    var description by TopicsTable.description
    var tags by TopicsTable.tags
    var icon by TopicsTable.icon
    var parent by TopicEntity optionalReferencedOn TopicsTable.parent
    val children by TopicEntity optionalReferrersOn TopicsTable.parent

    var locale by TopicsTable.locale

    fun toDto(): TopicDto = TopicDto(
        id = id.value,
        name = name,
        slug = slug,
        description = description,
        tags = tags?.split(",") ?: emptyList(),
        parentId = parent?.id?.value,
        icon = icon,
        childrenIds = children.map { it.id.value },
        locale = locale
    )
}

object UserTopicsTable : LongIdTable("user_topics") {
    val user = reference("user_id", UsersTable)
    val topic = reference("topic_id", TopicsTable)
    val level = enumerationByName("level", 20, TopicLevel::class)
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

    fun toDto(): UserTopicDto = UserTopicDto(
        id = id.value,
        userId = user.id.value,
        topic = topic.toDto(),
        level = level,
        description = description
    )
}
