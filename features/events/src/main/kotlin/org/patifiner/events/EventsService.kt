package org.patifiner.events

import org.patifiner.database.enums.ParticipantStatus
import org.patifiner.events.api.CreateEventRequest
import org.patifiner.events.api.EventsException.AccessDeniedException
import org.patifiner.events.api.EventsException.ActionNotAllowedException
import org.patifiner.events.api.UpdateEventRequest
import org.slf4j.Logger

internal class EventsService(
    val logger: Logger,
    private val eventsDao: EventsDao
) {

    suspend fun createEvent(creatorId: Long, request: CreateEventRequest): EventDto {
        logger.info("Creating event: ${request.title}")
        return eventsDao.create(creatorId, request)
    }

    suspend fun updateEvent(userId: Long, eventId: Long, request: UpdateEventRequest): EventDto {
        val event = eventsDao.getById(eventId)
        if (event.creator.id.value != userId) throw AccessDeniedException()
        return eventsDao.update(eventId, request)
    }

    suspend fun deleteEvent(userId: Long, eventId: Long) {
        val event = eventsDao.getById(eventId)
        if (event.creator.id.value != userId) throw AccessDeniedException()
        eventsDao.delete(eventId)
    }

    suspend fun joinEvent(userId: Long, eventId: Long): EventDto {
        return eventsDao.setParticipantStatus(eventId, userId, ParticipantStatus.JOINED)
    }

    suspend fun leaveEvent(userId: Long, eventId: Long): EventDto {
        val event = eventsDao.getById(eventId)
        if (event.creator.id.value == userId) {
            throw ActionNotAllowedException()
        }
        return eventsDao.setParticipantStatus(eventId, userId, ParticipantStatus.REJECTED)
    }

    suspend fun inviteUser(userId: Long, eventId: Long, targetUserId: Long): EventDto {
        val event = eventsDao.getById(eventId)
        if (event.creator.id.value != userId) throw AccessDeniedException()
        return eventsDao.setParticipantStatus(eventId, targetUserId, ParticipantStatus.INVITED)
    }
}
