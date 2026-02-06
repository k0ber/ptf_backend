package org.patifiner.topics.api

import io.ktor.http.HttpStatusCode
import org.patifiner.base.PtfException

sealed class TopicsException(message: String, code: String, statusCode: HttpStatusCode) : PtfException(message, code, statusCode) {

    class UserNotFoundException(topicId: Long) :
        TopicsException("User not found: $topicId", "USER_NOT_FOUND", HttpStatusCode.NotFound)

    class UserTopicAlreadyExistsException(userId: Long, topicId: Long) :
        TopicsException("User $userId already has topic $topicId", "TOPIC_ALREADY_EXISTS", HttpStatusCode.Conflict)

    class InvalidTopicName(topicName: String) :
        TopicsException("Incorrect topic name format: $topicName", "TOPIC_INVALID_NAME", HttpStatusCode.BadRequest)
}
