package org.patifiner.topics


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.patifiner.topics.api.AddUserTopicsRequest
import org.patifiner.topics.api.CreateTopicRequest
import org.patifiner.topics.api.TopicYaml
import org.patifiner.topics.api.TopicsYamlRoot
import org.patifiner.topics.data.TopicDao
import org.patifiner.topics.data.TopicEntity
import org.patifiner.topics.data.TopicsTable
import java.text.Normalizer

class TopicsService(private val topicDao: TopicDao) {

    suspend fun searchTopics(query: String) = topicDao.searchByName(query)

    suspend fun createTopic(request: CreateTopicRequest) = topicDao.create(request)

    suspend fun getBySlug(slug: String) = topicDao.getBySlug(slug)

    suspend fun addUserTopics(userId: Long, request: AddUserTopicsRequest): Set<UserTopicDto> =
        topicDao.addUserTopics(userId, request.topics)

    suspend fun removeUserTopics(userId: Long, topicIds: List<Long>): Long = topicDao.removeUserTopics(userId, topicIds)

    suspend fun getUserTopics(userId: Long): Set<UserTopicDto> = topicDao.getUserTopics(userId)

    suspend fun getTopicsTree(): List<TopicDto> = topicDao.getTopicsTree()

    // ---------- IMPORT ----------
    suspend fun importFromYaml(yamlText: String, overwrite: Boolean = true) {
        val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule.Builder().build())
        val root: TopicsYamlRoot = mapper.readValue(yamlText)

        newSuspendedTransaction {
            fun insertRecursively(node: TopicYaml, parent: TopicEntity? = null) {
                val existing = TopicEntity.find { (TopicsTable.slug eq (node.slug ?: "")) and (TopicsTable.locale eq root.locale) }.firstOrNull()

                val topic = existing ?: TopicEntity.new {
                    name = node.name
                    slug = node.slug ?: node.name.slugify()
                    description = node.description
                    tags = node.tags?.joinToString(",")
                    icon = node.name.take(2)
                    locale = root.locale
                    this.parent = parent
                }

                node.children?.forEach { insertRecursively(it, topic) }
            }

            root.topics.forEach { insertRecursively(it) }
        }
    }

    private fun String.slugify(): String {
        // Убираем emoji и спецсимволы, оставляем только буквы/цифры/пробелы/дефисы
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .replace("[^\\p{L}\\p{Nd}\\s-]".toRegex(), "") // оставляем буквы, цифры, пробелы и дефисы
            .trim()
            .lowercase()
            .replace("\\s+".toRegex(), "-") // заменяем пробелы на дефисы
            .replace("-+".toRegex(), "-")   // сжимаем повторяющиеся дефисы

        // Обрезаем, чтобы slug не был слишком длинным
        return normalized.take(64)
    }
}
