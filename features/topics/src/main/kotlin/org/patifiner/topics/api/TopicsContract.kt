package org.patifiner.topics.api

import org.patifiner.database.enums.TopicLevel

data class CreateTopicRequest(
    val name: String,
    val slug: String,
    val description: String? = null,
    val tags: List<String>? = null,
    val parentId: Long? = null,
    val icon: String? = null
)

data class AddUserTopicRequest(
    val topicId: Long,
    val level: TopicLevel,
    val description: String? = null
)

data class AddUserTopicsRequest(val topics: List<AddUserTopicRequest>)

data class RemoveUserTopicsRequest(val topicIds: List<Long>)
