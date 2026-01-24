package org.patifiner.events.api

import org.patifiner.database.enums.EventType
import org.patifiner.database.enums.ParticipantStatus

data class CreateEventRequest(
    val title: String,
    val description: String? = null,
    val type: EventType,
    val language: String = "ru", // todo: locale
    val cityId: Long? = null,
    val imageUrl: String? = null,
    val eventDate: String? = null,
    val schedule: String? = null,
    val topicIds: List<Long>
)

data class UpdateEventRequest(
    val title: String? = null,
    val description: String? = null,
    val type: EventType? = null,
    val language: String? = null,
    val cityId: Long? = null,
    val imageUrl: String? = null,
    val eventDate: String? = null,
    val schedule: String? = null,
    val topicIds: List<Long>? = null
)

data class InviteParticipantRequest(val targetUserId: Long)
