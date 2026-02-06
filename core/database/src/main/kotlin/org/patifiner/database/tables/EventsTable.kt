package org.patifiner.database.tables

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.patifiner.database.enums.EventType
import org.patifiner.database.enums.Language
import org.patifiner.database.enums.ParticipantStatus

object EventsTable : LongIdTable("events") {
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val type = enumerationByName("type", 20, EventType::class)
        .default(EventType.IRL_PUBLIC_PLACE)
    val language = enumerationByName("language", 10, Language::class)
        .default(Language.RU)
    val creator = reference("creator_id", UserTable)
    val city = reference("city_id", CitiesTable).nullable()
    val imageUrl = varchar("image_url", 511).nullable()
    val eventDate = datetime("event_date").nullable()
    val schedule = varchar("schedule", 255).nullable()
    val createdAt = datetime("created_at")
        .defaultExpression(CurrentDateTime)
}

class EventEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<EventEntity>(EventsTable)

    var title by EventsTable.title
    var description by EventsTable.description
    var type by EventsTable.type
    var language by EventsTable.language
    var creator by UserEntity referencedOn EventsTable.creator
    var city by CityEntity optionalReferencedOn EventsTable.city
    var imageUrl by EventsTable.imageUrl
    var eventDate by EventsTable.eventDate
    var schedule by EventsTable.schedule
    var createdAt by EventsTable.createdAt

    var topics by TopicEntity via EventTopicsTable
    val participants by UserParticipantEntity referrersOn EventParticipantsTable.event
}

object EventTopicsTable : LongIdTable("event_topics") {
    val event = reference("event_id", EventsTable)
    val topic = reference("topic_id", TopicsTable)
}

object EventParticipantsTable : LongIdTable("event_participants") {
    val event = reference("event_id", EventsTable)
    val user = reference("user_id", UserTable)
    val status = enumerationByName("status", 20, ParticipantStatus::class).default(ParticipantStatus.JOINED)

    init {
        uniqueIndex(event, user)
    }
}

class UserParticipantEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserParticipantEntity>(EventParticipantsTable)

    var event by EventEntity referencedOn EventParticipantsTable.event
    var user by UserEntity referencedOn EventParticipantsTable.user
    var status by EventParticipantsTable.status
}
