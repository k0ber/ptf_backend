package org.patifiner.upload.events

import io.ktor.http.HttpStatusCode
import org.patifiner.base.PatifinerException

sealed class EventsException(message: String, statusCode: HttpStatusCode) : PatifinerException(message, "EVENTS_ERROR", statusCode) {

    class MissingRequestField(fieldName: String) :
        EventsException("Missing request field: $fieldName", HttpStatusCode.BadRequest)
}
