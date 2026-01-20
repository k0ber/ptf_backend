package org.patifiner.user

import kotlinx.datetime.toJavaLocalDate
import org.patifiner.database.Gender
import org.patifiner.database.UserEntity
import org.patifiner.database.UserLanguage
import org.patifiner.user.api.CreateUserRequest
import java.time.LocalDate

class UserInfoDto(
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

fun UserEntity.toDto(): UserInfoDto = UserInfoDto(
    id = this.id.value,
    name = this.name,
    avatarUrl = this.avatarUrl,
    photos = this.photosList,
    birthDate = this.birthDate?.toJavaLocalDate(),
    email = this.email,
    cityId = this.city?.id?.value,
    cityName = this.city?.name,
    gender = this.gender,
    languages = this.languages
)

fun UserEntity.fromCreateRequest(req: CreateUserRequest, hashedPassword: String) {
    name = req.name
    email = req.email
    password = hashedPassword
}
