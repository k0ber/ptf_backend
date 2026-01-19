package org.patifiner.topics


import org.patifiner.topics.api.AddUserTopicsRequest
import org.patifiner.topics.api.CreateTopicRequest

class TopicsService(private val topicDao: TopicDao) {

    suspend fun searchTopics(query: String) = topicDao.searchByName(query)

    suspend fun createTopic(request: CreateTopicRequest) = topicDao.create(request)

    suspend fun getBySlug(slug: String) = topicDao.getBySlug(slug)

    suspend fun addUserTopics(userId: Long, request: AddUserTopicsRequest): Set<UserTopicDto> =
        topicDao.addUserTopics(userId, request.topics)

    suspend fun removeUserTopics(userId: Long, topicIds: List<Long>): Long = topicDao.removeUserTopics(userId, topicIds)

    suspend fun getUserTopics(userId: Long): Set<UserTopicDto> = topicDao.getUserTopics(userId)

    suspend fun getTopicsTree(): List<TopicDto> = topicDao.getTopicsTree()

}
