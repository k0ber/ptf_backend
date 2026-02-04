package org.patifiner.events.api

import org.patifiner.database.enums.EventType
import org.patifiner.database.enums.Language

data class CreateEventRequest(
    val title: String,
    val description: String? = null,
    val type: EventType,
    val language: Language = Language.RU,
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
    val language: Language? = null,
    val cityId: Long? = null,
    val imageUrl: String? = null,
    val eventDate: String? = null,
    val schedule: String? = null,
    val topicIds: List<Long>? = null
)

data class InviteParticipantRequest(val targetUserId: Long)
