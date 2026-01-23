package org.patifiner.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.patifiner.database.tables.*
import org.slf4j.Logger

private const val TOPICS_YAML = "/topics.yaml"
private const val CITIES_YAML = "/cities.yaml"
private data class CitiesListWrapper(val cities: List<CityInput>)
private data class CityInput(val name: String, val country: String)
private data class TopicsYamlRoot(val locale: String = "en", val topics: List<TopicYaml>)

private data class TopicYaml(
    val name: String,
    val slug: String? = null,
    val description: String? = null,
    val tags: List<String>? = null,
    val icon: String? = null,
    val children: List<TopicYaml>? = null
)

// creates new default tables from files each time application starts
class DatabaseInitializer(private val logger: Logger) {

    private val mapper = ObjectMapper(YAMLFactory())
        .registerModule(KotlinModule.Builder().build())

    suspend fun initData() {
        logger.info("Starting database initialization...")

        newSuspendedTransaction(Dispatchers.IO) {
            // 1. Управление схемой
            SchemaUtils.create(
                UserTable,
                CitiesTable,
                TopicsTable,
                UserTopicsTable,
                UserRelationsTable,
                EventsTable,
                EventTopicsTable,
                EventParticipantsTable
            )

            // 2. Специфичные для БД расширения
            exec("CREATE EXTENSION IF NOT EXISTS pg_trgm")

            // 3. Синхронизация данных
            loadCities()
            loadTopics()
        }

        logger.info("Database initialization finished successfully.")
    }

    private fun loadCities() {
        val stream = this::class.java.getResourceAsStream(CITIES_YAML)
            ?: return logger.warn("Resource not found: $CITIES_YAML")

        stream.use {
            val data = mapper.readValue<CitiesListWrapper>(it)
            data.cities.forEach { input ->
                val exists = !CityEntity.find { CitiesTable.name eq input.name }.empty()
                if (!exists) {
                    CityEntity.new {
                        name = input.name
                        country = input.country
                    }
                    logger.debug("City added: ${input.name}")
                }
            }
        }
    }

    private fun loadTopics() {
        val stream = this::class.java.getResourceAsStream(TOPICS_YAML)
            ?: return logger.warn("Resource not found: $TOPICS_YAML")

        stream.use {
            val root = mapper.readValue<TopicsYamlRoot>(it)

            fun insertRecursively(node: TopicYaml, parent: TopicEntity? = null) {
                val slugValue = node.slug ?: node.name.lowercase().replace(" ", "_")

                // Ищем существующий топик по slug и локали
                val existing = TopicEntity.find {
                    (TopicsTable.slug eq slugValue) and (TopicsTable.locale eq root.locale)
                }.firstOrNull()

                val topic = existing ?: TopicEntity.new {
                    name = node.name
                    slug = slugValue
                    description = node.description
                    tags = node.tags?.joinToString(",")
                    icon = node.icon ?: node.name.take(1)
                    locale = root.locale
                    this.parent = parent
                }

                node.children?.forEach { insertRecursively(it, topic) }
            }

            root.topics.forEach { insertRecursively(it) }
            logger.info("Topics synchronization for locale '${root.locale}' completed.")
        }
    }
}

/**
 *
 *     // ---------- IMPORT ----------
 *     suspend fun importFromYaml(yamlText: String, overwrite: Boolean = true) {
 *         val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule.Builder().build())
 *         val root: TopicsYamlRoot = mapper.readValue(yamlText)
 *
 *         newSuspendedTransaction {
 *             fun insertRecursively(node: TopicYaml, parent: TopicEntity? = null) {
 *                 val existing = TopicEntity.find { (TopicsTable.slug eq (node.slug ?: "")) and (TopicsTable.locale eq root.locale) }.firstOrNull()
 *
 *                 val topic = existing ?: TopicEntity.new {
 *                     name = node.name
 *                     slug = node.slug ?: node.name.slugify()
 *                     description = node.description
 *                     tags = node.tags?.joinToString(",")
 *                     icon = node.name.take(2)
 *                     locale = root.locale
 *                     this.parent = parent
 *                 }
 *
 *                 node.children?.forEach { insertRecursively(it, topic) }
 *             }
 *
 *             root.topics.forEach { insertRecursively(it) }
 *         }
 *     }
 *
 *     private fun String.slugify(): String {
 *         // Убираем emoji и спецсимволы, оставляем только буквы/цифры/пробелы/дефисы
 *         val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
 *             .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
 *             .replace("[^\\p{L}\\p{Nd}\\s-]".toRegex(), "") // оставляем буквы, цифры, пробелы и дефисы
 *             .trim()
 *             .lowercase()
 *             .replace("\\s+".toRegex(), "-") // заменяем пробелы на дефисы
 *             .replace("-+".toRegex(), "-")   // сжимаем повторяющиеся дефисы
 *
 *         // Обрезаем, чтобы slug не был слишком длинным
 *         return normalized.take(64)
 *     }
 */