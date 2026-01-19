package org.patifiner.database

data class TopicsYamlRoot(
    val locale: String = "en",
    val topics: List<TopicYaml>
)

data class TopicYaml(
    val name: String,
    val slug: String? = null,
    val description: String? = null,
    val tags: List<String>? = null,
    val icon: String? = null,
    val children: List<TopicYaml>? = null
)
