package org.patifiner.user

import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.patifiner.database.UserEntity
import org.patifiner.user.api.CreateUserRequest
import java.time.LocalDate

class UserInfoDto(
    val id: Long,
    val name: String,
    val avatarUrl: String? = null,
    val photos: List<String> = emptyList(),
    val birthDate: LocalDate?,
    val email: String,
)

fun UserEntity.toDto(): UserInfoDto = UserInfoDto(
    id = this.id.value,
    name = this.name,
    avatarUrl = this.avatarUrl,
    photos = this.photosList,
    birthDate = this.birthDate?.toJavaLocalDate(),
    email = this.email,
)

fun UserEntity.fromDto(req: CreateUserRequest, hashedPassword: String) {
    name = req.name
    birthDate = req.birthDate?.toKotlinLocalDate()
    email = req.email
    password = hashedPassword
}
