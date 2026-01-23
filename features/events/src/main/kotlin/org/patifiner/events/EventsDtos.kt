package org.patifiner.events

import kotlinx.datetime.LocalDateTime
import org.patifiner.database.enums.EventType
import org.patifiner.database.enums.ParticipantStatus
import org.patifiner.database.tables.EventEntity
import org.patifiner.database.tables.UserParticipantEntity
import org.patifiner.user.UserDto
import org.patifiner.user.toDto

data class EventDto(
    val id: Long,
    val title: String,
    val description: String?,
    val type: EventType,
    val language: String,
    val creator: UserDto,
    val cityId: Long?,
    val cityName: String?,
    val imageUrl: String?,
    val eventDate: LocalDateTime?,
    val schedule: String?,
    val topics: List<Long>,
    val participants: List<EventParticipantDto>
)

data class EventParticipantDto(
    val user: UserDto,
    val status: ParticipantStatus
)

fun EventEntity.toDto(): EventDto = EventDto(
    id = this.id.value,
    title = this.title,
    description = this.description,
    type = this.type,
    language = this.language,
    creator = this.creator.toDto(),
    cityId = this.city?.id?.value,
    cityName = this.city?.name,
    imageUrl = this.imageUrl,
    eventDate = this.eventDate,
    schedule = this.schedule,
    topics = this.topics.map { it.id.value },
    participants = this.participants.map { it.toDto() }
)

fun UserParticipantEntity.toDto(): EventParticipantDto = EventParticipantDto(
    user = this.user.toDto(),
    status = this.status
)
