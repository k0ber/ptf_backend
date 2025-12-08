package org.patifiner.topics.api

sealed class TopicsException(message: String) : RuntimeException(message) {

    class TopicNotFoundException(topicId: Long) : TopicsException("Topic not found: $topicId")

    class UserTopicAlreadyExistsException(userId: Long, topicId: Long) :
        TopicsException("User $userId already has topic $topicId")

    class InvalidTopicName(topicName: String) :
        TopicsException("Incorrect topic name format: $topicName")

}
