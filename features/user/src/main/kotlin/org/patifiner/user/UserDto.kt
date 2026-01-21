package org.patifiner.user

import kotlinx.datetime.LocalDate
import org.patifiner.database.enums.Gender
import org.patifiner.database.enums.UserLanguage
import org.patifiner.database.tables.CityEntity
import org.patifiner.database.tables.UserEntity
import org.patifiner.user.api.CreateUserRequest
import org.patifiner.user.api.UpdateUserRequest

class UserDto(
    val id: Long,
    val name: String,
    val avatarUrl: String? = null,
    val photos: List<String> = emptyList(),
    val birthDate: LocalDate?,
    val email: String,
    val cityId: Long? = null,
    val cityName: String? = null,
    val gender: Gender = Gender.NOT_SPECIFIED,
    val languages: List<UserLanguage> = emptyList()
)

fun UserEntity.toDto(): UserDto = UserDto(
    id = this.id.value,
    name = this.name,
    avatarUrl = this.avatarUrl,
    photos = this.photos,
    birthDate = this.birthDate,
    email = this.email,
    cityId = this.city?.id?.value,
    cityName = this.city?.name,
    gender = this.gender,
    languages = this.languages
)

fun UserEntity.fromCreateRequest(req: CreateUserRequest, hashedPassword: String) {
    this.name = req.name
    this.email = req.email
    this.password = hashedPassword
}

fun UserEntity.fromUpdateRequest(req: UpdateUserRequest, city: CityEntity?) {
    this.name = req.name
    req.birthDate?.let { dateStr -> this.birthDate = LocalDate.parse(dateStr) }
    this.gender = req.gender
    this.languages = req.languages
    this.city = city
}
