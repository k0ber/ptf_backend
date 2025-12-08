package org.patifiner.user.data

import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.patifiner.user.UserInfoDto
import org.patifiner.user.api.CreateUserRequest

object UsersTable : LongIdTable("user") {
    val name = varchar("name", 50)
    val surname = varchar("surname", 50)
    val avatarUrl = varchar("avatar_url", 255).nullable()

    val birthDate = date("birth_date")
    val email = varchar("email", 50).uniqueIndex()
    val password = varchar("password", 100)
}

class UserEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserEntity>(UsersTable)

    var name by UsersTable.name
    var surname by UsersTable.surname
    var avatarUrl by UsersTable.avatarUrl

    var birthDate by UsersTable.birthDate
    var email by UsersTable.email
    var password by UsersTable.password

    /** ORM → DTO */
    fun toDto(): UserInfoDto = UserInfoDto(
        id = this.id.value,
        name = this.name,
        avatarUrl = this.avatarUrl,

        surname = this.surname,
        birthDate = this.birthDate.toJavaLocalDate(),
        email = this.email,
    )

    /** Заполнить сущность из DTO + уже посчитанного хеша пароля */
    fun fromDto(req: CreateUserRequest, hashedPassword: String) {
        name = req.name
        surname = req.surname
        birthDate = req.birthDate.toKotlinLocalDate()
        email = req.email
        password = hashedPassword
    }
}
