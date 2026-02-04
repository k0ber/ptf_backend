package org.patifiner.events.api

import io.ktor.http.HttpStatusCode
import org.patifiner.base.PtfException

sealed class EventsException(message: String, code: String, statusCode: HttpStatusCode) : PtfException(message, code, statusCode) {
    class EventNotFoundException(id: Long) : EventsException("Event with id $id not found", "EVENT_NOT_FOUND", HttpStatusCode.NotFound)
    class AccessDeniedException : EventsException("You are not the creator of this event", "EVENT_ACCESS_DENIED", HttpStatusCode.Forbidden)
    class ActionNotAllowedException() : EventsException("Creator cannot leave the event. Delete it instead.", "EVENT_ACTION_NOT_ALLOWED", HttpStatusCode.BadRequest)
}
