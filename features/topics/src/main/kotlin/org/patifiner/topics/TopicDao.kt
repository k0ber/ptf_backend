package org.patifiner.topics

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
    fun searchByName(rawQuery: String): List<TopicDto>
    fun getBySlug(slug: String): TopicDto?
    fun create(request: CreateTopicRequest): TopicDto
    fun addUserTopics(userId: Long, requests: List<AddUserTopicRequest>): Set<UserTopicDto>
    fun removeUserTopics(userId: Long, topicIds: List<Long>): Long
    fun getUserTopics(userId: Long): Set<UserTopicDto>
    fun getTopicsTree(): List<TopicDto>

    fun findUserIdsByAnyTopics(
        topicIds: Collection<Long>,
        excludeUserIds: Collection<Long>,
        pagedRequest: PagedRequest
    ): List<Long>

    fun getRandomUserIdByAnyTopics(
        topicIds: Collection<Long>,
        excludeUserIds: Collection<Long>
    ): Long?
}

class ExposedTopicDao : TopicDao {

    override fun searchByName(rawQuery: String): List<TopicDto> =
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

    override fun create(request: CreateTopicRequest): TopicDto {
        val foundParent = request.parentId?.let { TopicEntity.findById(it) }
        val topic = TopicEntity.new {
            name = request.name
            slug = request.slug
            description = request.description
            tags = request.tags?.joinToString(",")
            icon = request.icon
            parent = foundParent
        }
        return topic.toDto()
    }

    override fun addUserTopics(userId: Long, requests: List<AddUserTopicRequest>): Set<UserTopicDto> {
        val user = UserEntity.findById(userId) ?: throw TopicsException.UserNotFoundException(userId)

        return requests.map { req ->
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

    override fun removeUserTopics(userId: Long, topicIds: List<Long>): Long {
        val toDelete = UserTopicEntity.find {
            (UserTopicsTable.user eq EntityID(userId, UserTable)) and
                    (UserTopicsTable.topic inList topicIds.map { EntityID(it, TopicsTable) })
        }
        val count = toDelete.count()
        UserTopicsTable.deleteWhere {
            (user eq EntityID(userId, UserTable)) and (topic inList topicIds)
        }
        return count
    }

    override fun getUserTopics(userId: Long): Set<UserTopicDto> =
        UserTopicEntity.find { UserTopicsTable.user eq EntityID(userId, UserTable) }
            .map { it.toDto() }
            .toSet()

    override fun getTopicsTree(): List<TopicDto> {
        val allEntities = TopicEntity.all().with(TopicEntity::parent)

        val allDtos = allEntities.map { it.toDto() }
        val groupedByParent = allDtos.groupBy { it.parentId }

        return allDtos.map { currentDto ->
            val childrenIds = groupedByParent[currentDto.id]?.map { it.id } ?: emptyList()
            if (childrenIds.isNotEmpty()) currentDto.copy(childrenIds = childrenIds) else currentDto
        }
    }

    override fun findUserIdsByAnyTopics(
        topicIds: Collection<Long>,
        excludeUserIds: Collection<Long>,
        pagedRequest: PagedRequest
    ): List<Long> {
        if (topicIds.isEmpty()) return emptyList()

        val offsetValue = calculateOffset(pagedRequest.page, pagedRequest.perPage)

        return UserTopicsTable
            .slice(UserTopicsTable.user)
            .select {
                (UserTopicsTable.topic inList topicIds) and
                        (UserTopicsTable.user notInList excludeUserIds)
            }
            .groupBy(UserTopicsTable.user)
            .limit(n = pagedRequest.perPage, offset = offsetValue)
            .map { it[UserTopicsTable.user].value }
    }

    override fun getBySlug(slug: String): TopicDto? = TopicEntity.find { TopicsTable.slug eq slug }.firstOrNull()?.toDto()

    override fun getRandomUserIdByAnyTopics(topicIds: Collection<Long>, excludeUserIds: Collection<Long>): Long? {
        if (topicIds.isEmpty()) return null

        return UserTopicsTable
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
