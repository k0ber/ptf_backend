package org.patifiner.database.tables

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object TopicsTable : LongIdTable("topics") {
    val name = varchar("name", 255)
    val slug = varchar("slug", 255).uniqueIndex()
    val description = text("description").nullable()
    val tags = text("tags").nullable()
    val icon = varchar("icon", 32).nullable()
    val parent = reference("parent_id", TopicsTable).nullable()
    val locale = varchar("locale", 8).default("en")
}

class TopicEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<TopicEntity>(TopicsTable)

    var name by TopicsTable.name
    var slug by TopicsTable.slug
    var description by TopicsTable.description
    var tags by TopicsTable.tags
    var icon by TopicsTable.icon
    var parent by TopicEntity optionalReferencedOn TopicsTable.parent
    val children by TopicEntity optionalReferrersOn TopicsTable.parent

    var locale by TopicsTable.locale
}
