package org.patifiner.topics


import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.patifiner.topics.api.AddUserTopicsRequest
import org.patifiner.topics.api.CreateTopicRequest

class TopicsService(private val topicDao: TopicDao) {

    suspend fun searchTopics(query: String) = newSuspendedTransaction(Dispatchers.IO) {
        topicDao.searchByName(query)
    }

    suspend fun createTopic(request: CreateTopicRequest) = newSuspendedTransaction(Dispatchers.IO) {
        topicDao.create(request)
    }

    suspend fun getBySlug(slug: String) = newSuspendedTransaction(Dispatchers.IO) {
        topicDao.getBySlug(slug)
    }

    suspend fun addUserTopics(userId: Long, request: AddUserTopicsRequest): Set<UserTopicDto> = newSuspendedTransaction(Dispatchers.IO) {
        topicDao.addUserTopics(userId, request.topics)
    }

    suspend fun removeUserTopics(userId: Long, topicIds: List<Long>): Long = newSuspendedTransaction(Dispatchers.IO) {
        topicDao.removeUserTopics(userId, topicIds)
    }

    suspend fun getUserTopics(userId: Long): Set<UserTopicDto> = newSuspendedTransaction(Dispatchers.IO) {
        topicDao.getUserTopics(userId)
    }

    suspend fun getTopicsTree(): List<TopicDto> = newSuspendedTransaction(Dispatchers.IO) {
        topicDao.getTopicsTree()
    }

    suspend fun getTopicsByTags(): List<TagGroupDto> = newSuspendedTransaction(Dispatchers.IO) {
        val allTopics = topicDao.getAllTopics()
        val tagMap = mutableMapOf<String, MutableList<TopicDto>>()

        allTopics.forEach { topic ->
            topic.tags.forEach { tag ->
                tagMap.getOrPut(tag) { mutableListOf() }.add(topic)
            }
        }

        tagMap.map { (tag, topics) -> TagGroupDto(tag, topics) }
            .sortedByDescending { it.topics.size } // Сначала самые популярные теги
    }

}
