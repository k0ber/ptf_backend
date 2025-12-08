package org.patifiner.topics


enum class TopicLevel {
    NONE,
    NEWBIE,
    INTERMEDIATE,
    ADVANCED,
}

data class TopicDto(
    val locale: String,
    val id: Long,
    val name: String,
    val slug: String,
    val description: String?,
    val tags: List<String>?,
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
