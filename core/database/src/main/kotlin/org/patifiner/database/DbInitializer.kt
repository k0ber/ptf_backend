package org.patifiner.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.patifiner.base.configurePtfDefaults
import org.patifiner.database.enums.Language
import org.patifiner.database.tables.CitiesTable
import org.patifiner.database.tables.CityEntity
import org.patifiner.database.tables.EventParticipantsTable
import org.patifiner.database.tables.EventTopicsTable
import org.patifiner.database.tables.EventsTable
import org.patifiner.database.tables.TopicEntity
import org.patifiner.database.tables.TopicsTable
import org.patifiner.database.tables.UserRelationsTable
import org.patifiner.database.tables.UserTable
import org.patifiner.database.tables.UserTopicsTable
import org.slf4j.Logger

private const val TOPICS_YAML = "/topics.yaml"
private const val CITIES_YAML = "/cities.yaml"

private data class CitiesListWrapper(val cities: List<CityInput>)
private data class CityInput(val name: String, val country: String)

private data class TopicsYamlRoot(
    val locale: Language = Language.EN,
    val topics: List<TopicYaml>
)

private data class TopicYaml(
    val name: String,
    val slug: String? = null,
    val description: String? = null,
    val tags: List<String>? = null,
    val icon: String? = null,
    val children: List<TopicYaml>? = null
)

// creates new default tables from files each time application starts
class DbInitializer(private val logger: Logger) {

    private val yamlMapper = ObjectMapper(YAMLFactory()).configurePtfDefaults()

    suspend fun initData() {
        logger.info("Starting database initialization...")

        newSuspendedTransaction(Dispatchers.IO) {
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

            exec("CREATE EXTENSION IF NOT EXISTS pg_trgm")

            loadCities()
            loadTopics()
        }

        logger.info("Database initialization finished successfully.")
    }

    private fun loadCities() {
        val stream = this::class.java.getResourceAsStream(CITIES_YAML)
            ?: return logger.warn("Resource not found: $CITIES_YAML")

        stream.use {
            val data = yamlMapper.readValue<CitiesListWrapper>(it)
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
            val root = try {
                yamlMapper.readValue<TopicsYamlRoot>(it)
            } catch (e: Exception) {
                logger.error("Failed to parse topics.yaml}", e)
                return
            }

            val localeEnum = root.locale

            fun insertRecursively(node: TopicYaml, parent: TopicEntity? = null) {
                val slugValue = node.slug ?: node.name.lowercase().replace(" ", "_")

                val existing = TopicEntity.find {
                    (TopicsTable.slug eq slugValue) and (TopicsTable.locale eq localeEnum)
                }.firstOrNull()

                val topic = existing ?: TopicEntity.new {
                    name = node.name
                    slug = slugValue
                    description = node.description
                    tags = node.tags?.joinToString(",")
                    icon = node.icon ?: node.name.take(1)
                    locale = localeEnum
                    this.parent = parent
                }

                node.children?.forEach { insertRecursively(it, topic) }
            }

            root.topics.forEach { insertRecursively(it) }
            logger.info("Topics synchronization for locale '${localeEnum}' completed.")
        }
    }
}
