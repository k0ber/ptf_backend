package org.patifiner.database.tables

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object EventsTable : LongIdTable("events") {
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val city = reference("city_id", CitiesTable)
    val topic = reference("topic_id", TopicsTable)
    val creator = reference("creator_id", UserTable)
    val imageUrl = varchar("image_url", 511).nullable()
    val eventDate = datetime("event_date").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

class EventEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<EventEntity>(EventsTable)

    var title by EventsTable.title
    var description by EventsTable.description
    var city by CityEntity referencedOn EventsTable.city
    var topic by TopicEntity referencedOn EventsTable.topic
    var creator by UserEntity referencedOn EventsTable.creator
    var imageUrl by EventsTable.imageUrl
    var eventDate by EventsTable.eventDate
    var createdAt by EventsTable.createdAt
}
