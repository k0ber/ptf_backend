package org.patifiner.topics

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.Random
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.patifiner.base.PagedRequest
import org.patifiner.base.calculateOffset
import org.patifiner.database.tables.TopicEntity
import org.patifiner.database.tables.TopicsTable
import org.patifiner.database.tables.UserEntity
import org.patifiner.database.tables.UserTable
import org.patifiner.database.tables.UserTopicEntity
import org.patifiner.database.tables.UserTopicsTable
import org.patifiner.postgres.similarity
import org.patifiner.postgres.trgmSearch
import org.patifiner.topics.api.AddUserTopicRequest
import org.patifiner.topics.api.CreateTopicRequest
import org.patifiner.topics.api.TopicsException

interface TopicDao {
    suspend fun searchByName(rawQuery: String): List<TopicDto>
    suspend fun getBySlug(slug: String): TopicDto?
    suspend fun create(request: CreateTopicRequest): TopicDto
    suspend fun addUserTopics(userId: Long, requests: List<AddUserTopicRequest>): Set<UserTopicDto>
    suspend fun removeUserTopics(userId: Long, topicIds: List<Long>): Long
    suspend fun getUserTopics(userId: Long): Set<UserTopicDto>
    suspend fun getTopicsTree(): List<TopicDto>

    suspend fun findUserIdsByAnyTopics(
        topicIds: Collection<Long>,
        excludeUserIds: Collection<Long>,
        pagedRequest: PagedRequest
    ): List<Long>

    suspend fun getRandomUserIdByAnyTopics(
        topicIds: Collection<Long>,
        excludeUserIds: Collection<Long>
    ): Long?
}

class ExposedTopicDao : TopicDao {

    override suspend fun searchByName(rawQuery: String): List<TopicDto> =
        newSuspendedTransaction(Dispatchers.IO) {
            rawQuery.trim().takeIf(String::isNotEmpty)?.let { query ->
                val likeResults = TopicEntity.find { TopicsTable.name.lowerCase() like "%${query.lowercase()}%" }.limit(50)
                val entitiesToMap =
                    if (likeResults.count() > 0) likeResults
                    else { // trgm search if like approack is empty
                        TopicEntity.find { TopicsTable.name trgmSearch query }
                            .orderBy(TopicsTable.name.similarity(query) to SortOrder.DESC)
                            .limit(50)
                    }
                entitiesToMap.map { it.toDto() }
            } ?: emptyList()
        }

    override suspend fun create(request: CreateTopicRequest): TopicDto =
        newSuspendedTransaction(Dispatchers.IO) {
            val foundParent = request.parentId?.let { TopicEntity.findById(it) }
            val topic = TopicEntity.new {
                name = request.name
                slug = request.slug
                description = request.description
                tags = request.tags?.joinToString(",")
                icon = request.icon
                parent = foundParent
            }
            topic.toDto()
        }

    override suspend fun addUserTopics(userId: Long, requests: List<AddUserTopicRequest>): Set<UserTopicDto> =
        newSuspendedTransaction(Dispatchers.IO) {
            val user = UserEntity.findById(userId) ?: throw TopicsException.UserNotFoundException(userId)

            requests.map { req ->
                val topic = TopicEntity.findById(req.topicId) ?: throw TopicsException.UserNotFoundException(req.topicId)

                val existing = UserTopicEntity.find {
                    (UserTopicsTable.user eq user.id) and (UserTopicsTable.topic eq topic.id)
                }.firstOrNull()

                val entity = if (existing != null) {
                    existing.level = req.level
                    existing.description = req.description
                    existing
                } else {
                    UserTopicEntity.new {
                        this.user = user
                        this.topic = topic
                        this.level = req.level
                        this.description = req.description
                    }
                }

                entity.toDto()
            }.toSet()
        }

    override suspend fun removeUserTopics(userId: Long, topicIds: List<Long>): Long =
        newSuspendedTransaction(Dispatchers.IO) {
            val toDelete = UserTopicEntity.find {
                (UserTopicsTable.user eq EntityID(userId, UserTable)) and
                        (UserTopicsTable.topic inList topicIds.map { EntityID(it, TopicsTable) })
            }
            val count = toDelete.count()
            UserTopicsTable.deleteWhere {
                (user eq EntityID(userId, UserTable)) and (topic inList topicIds)
            }
            count
        }

    override suspend fun getUserTopics(userId: Long): Set<UserTopicDto> =
        newSuspendedTransaction(Dispatchers.IO) {
            UserTopicEntity.find { UserTopicsTable.user eq EntityID(userId, UserTable) }
                .map { it.toDto() }
                .toSet()
        }

    override suspend fun getTopicsTree(): List<TopicDto> =
        newSuspendedTransaction(Dispatchers.IO) {
            val allEntities = TopicEntity.all().with(TopicEntity::parent)

            val allDtos = allEntities.map { it.toDto() }
            val groupedByParent = allDtos.groupBy { it.parentId }

            allDtos.map { currentDto ->
                val childrenIds = groupedByParent[currentDto.id]?.map { it.id } ?: emptyList()
                if (childrenIds.isNotEmpty()) currentDto.copy(childrenIds = childrenIds) else currentDto
            }
        }

    override suspend fun findUserIdsByAnyTopics(
        topicIds: Collection<Long>,
        excludeUserIds: Collection<Long>,
        pagedRequest: PagedRequest
    ): List<Long> = newSuspendedTransaction(Dispatchers.IO) {
        if (topicIds.isEmpty()) return@newSuspendedTransaction emptyList()

        val offsetValue = calculateOffset(pagedRequest.page, pagedRequest.perPage)

        UserTopicsTable
            .slice(UserTopicsTable.user)
            .select {
                (UserTopicsTable.topic inList topicIds) and
                        (UserTopicsTable.user notInList excludeUserIds)
            }
            .groupBy(UserTopicsTable.user)
            .limit(n = pagedRequest.perPage, offset = offsetValue)
            .map { it[UserTopicsTable.user].value }
    }

    override suspend fun getBySlug(slug: String): TopicDto? =
        newSuspendedTransaction(Dispatchers.IO) {
            TopicEntity.find { TopicsTable.slug eq slug }.firstOrNull()?.toDto()
        }

    override suspend fun getRandomUserIdByAnyTopics(
        topicIds: Collection<Long>,
        excludeUserIds: Collection<Long>
    ): Long? = newSuspendedTransaction(Dispatchers.IO) {
        if (topicIds.isEmpty()) return@newSuspendedTransaction null

        UserTopicsTable
            .slice(UserTopicsTable.user)
            .select {
                (UserTopicsTable.topic inList topicIds) and (UserTopicsTable.user notInList excludeUserIds)
            }
            .groupBy(UserTopicsTable.user)
            .orderBy(Random())
            .limit(1)
            .map { it[UserTopicsTable.user].value }
            .singleOrNull()
    }

}
