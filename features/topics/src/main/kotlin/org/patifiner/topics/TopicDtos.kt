package org.patifiner.topics

import org.patifiner.database.enums.Language
import org.patifiner.database.tables.TopicEntity
import org.patifiner.database.enums.TopicLevel
import org.patifiner.database.tables.UserTopicEntity

data class TopicDto(
    val locale: Language,
    val id: Long,
    val name: String,
    val slug: String,
    val description: String?,
    val tags: List<String>,
    val icon: String?,
    val parentId: Long?,
    val childrenIds: List<Long> = emptyList()
)

data class UserTopicDto(
    val id: Long,
    val userId: Long,
    val topic: TopicDto,
    val level: TopicLevel,
    val description: String?,
)

data class TagGroupDto(
    val tag: String,
    val topics: List<TopicDto>
)

fun TopicEntity.toDto(): TopicDto = TopicDto(
    id = id.value,
    name = name,
    slug = slug,
    description = description,
    tags = tags,
    parentId = parent?.id?.value,
    icon = icon,
    childrenIds = children.map { it.id.value },
    locale = locale
)

fun UserTopicEntity.toDto(): UserTopicDto = UserTopicDto(
    id = id.value,
    userId = user.id.value,
    topic = topic.toDto(),
    level = level,
    description = description
)
