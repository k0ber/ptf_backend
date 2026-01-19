package org.patifiner.events.api

import org.patifiner.events.EventType

data class CreateEventRequest(
    val eventName: String,
    val eventType: EventType,
)

data class UploadResponse(
    val url: String,
    val filename: String,
    val sizeBytes: Long,
)
