package org.patifiner.search

import org.patifiner.topics.TopicDto
import org.patifiner.topics.UserTopicDto
import org.patifiner.user.UserInfoDto


data class UserProfileDto(
    val userInfo: UserInfoDto,
    val userTopics: Set<UserTopicDto>
)

data class TopicIdeaDto(
    val person: UserProfileDto,
    val topic: TopicDto,
    val idea: String
)
