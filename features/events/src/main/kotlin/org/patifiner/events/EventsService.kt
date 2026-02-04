package org.patifiner.events

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
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

    suspend fun createEvent(creatorId: Long, request: CreateEventRequest): EventDto = newSuspendedTransaction(Dispatchers.IO) {
        logger.info("Creating event: ${request.title}")
        eventsDao.create(creatorId, request)
    }

    suspend fun updateEvent(userId: Long, eventId: Long, request: UpdateEventRequest): EventDto = newSuspendedTransaction(Dispatchers.IO) {
        val event = eventsDao.getById(eventId)
        if (event.creator.id.value != userId) throw AccessDeniedException()
        eventsDao.update(eventId, request)
    }

    suspend fun deleteEvent(userId: Long, eventId: Long) = newSuspendedTransaction(Dispatchers.IO) {
        val event = eventsDao.getById(eventId)
        if (event.creator.id.value != userId) throw AccessDeniedException()
        eventsDao.delete(eventId)
    }

    suspend fun joinEvent(userId: Long, eventId: Long): EventDto = newSuspendedTransaction(Dispatchers.IO) {
        eventsDao.setParticipantStatus(eventId, userId, ParticipantStatus.JOINED)
    }

    suspend fun leaveEvent(userId: Long, eventId: Long): EventDto = newSuspendedTransaction(Dispatchers.IO) {
        val event = eventsDao.getById(eventId)
        if (event.creator.id.value == userId) {
            throw ActionNotAllowedException()
        }
        eventsDao.setParticipantStatus(eventId, userId, ParticipantStatus.REJECTED)
    }

    suspend fun inviteUser(userId: Long, eventId: Long, targetUserId: Long): EventDto = newSuspendedTransaction(Dispatchers.IO) {
        val event = eventsDao.getById(eventId)
        if (event.creator.id.value != userId) throw AccessDeniedException()
        eventsDao.setParticipantStatus(eventId, targetUserId, ParticipantStatus.INVITED)
    }
}
