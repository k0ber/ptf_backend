package org.patifiner.events

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.and
import org.patifiner.database.enums.ParticipantStatus
import org.patifiner.database.tables.CityEntity
import org.patifiner.database.tables.EventEntity
import org.patifiner.database.tables.EventParticipantsTable
import org.patifiner.database.tables.TopicEntity
import org.patifiner.database.tables.TopicsTable
import org.patifiner.database.tables.UserEntity
import org.patifiner.database.tables.UserParticipantEntity
import org.patifiner.events.api.CreateEventRequest
import org.patifiner.events.api.EventsException.EventNotFoundException
import org.patifiner.events.api.UpdateEventRequest

interface EventsDao {
    fun create(creatorId: Long, request: CreateEventRequest): EventDto
    fun update(eventId: Long, request: UpdateEventRequest): EventDto
    fun delete(eventId: Long)
    fun getById(eventId: Long): EventEntity
    fun setParticipantStatus(eventId: Long, userId: Long, status: ParticipantStatus): EventDto
}

internal class ExposedEventsDao : EventsDao {

    override fun create(creatorId: Long, request: CreateEventRequest): EventDto {
        val event = EventEntity.new {
            title = request.title
            description = request.description
            type = request.type
            language = request.language
            creator = UserEntity[creatorId]
            city = request.cityId?.let { CityEntity[it] }
            imageUrl = request.imageUrl
            eventDate = request.eventDate?.let { LocalDateTime.parse(it) }
            schedule = request.schedule
            topics = TopicEntity.find { TopicsTable.id inList request.topicIds }
        }

        // Создатель автоматически становится участником
        UserParticipantEntity.new {
            this.event = event
            this.user = UserEntity[creatorId]
            this.status = ParticipantStatus.JOINED
        }

        return event.toDto()
    }

    override fun getById(eventId: Long): EventEntity = EventEntity.findById(eventId) ?: throw EventNotFoundException(eventId)

    override fun update(eventId: Long, request: UpdateEventRequest): EventDto {
        val event = EventEntity.findById(eventId) ?: throw EventNotFoundException(eventId)

        request.title?.let { event.title = it }
        request.description?.let { event.description = it }
        request.type?.let { event.type = it }
        request.language?.let { event.language = it }
        request.cityId?.let { event.city = CityEntity[it] }
        request.imageUrl?.let { event.imageUrl = it }
        request.eventDate?.let { event.eventDate = LocalDateTime.parse(it) }
        request.schedule?.let { event.schedule = it }
        request.topicIds?.let { ids -> event.topics = TopicEntity.find { TopicsTable.id inList ids } }

        return event.toDto()
    }

    override fun delete(eventId: Long) = EventEntity.findById(eventId)?.delete() ?: throw EventNotFoundException(eventId)

    override fun setParticipantStatus(eventId: Long, userId: Long, status: ParticipantStatus): EventDto {
        val participant = UserParticipantEntity.find {
            (EventParticipantsTable.event eq eventId) and (EventParticipantsTable.user eq userId)
        }.singleOrNull() ?: UserParticipantEntity.new {
            this.event = EventEntity[eventId]
            this.user = UserEntity[userId]
        }

        participant.status = status

        return EventEntity[eventId].toDto()
    }
}
