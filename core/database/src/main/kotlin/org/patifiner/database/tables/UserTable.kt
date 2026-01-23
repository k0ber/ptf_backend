package org.patifiner.database.tables

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.patifiner.database.DatabaseConfig
import org.patifiner.database.enums.Gender
import org.patifiner.database.enums.UserLanguage

object UserTable : LongIdTable("user") {
    val name = varchar("name", 50)
    val cityId = reference("city_id", CitiesTable).nullable()
    val avatarUrl = varchar("avatar_url", 255).nullable()
    val photos = jsonb<List<String>>("photos", DatabaseConfig.json).default(emptyList())
    val birthDate = date("birth_date").nullable()
    val email = varchar("email", 50).uniqueIndex()
    val password = varchar("password", 100)
    val gender = enumerationByName("gender", 20, Gender::class).default(Gender.NOT_SPECIFIED)
    val languages = jsonb<List<UserLanguage>>("languages", DatabaseConfig.json).default(emptyList())
    val locale = varchar("locale", 10).default("en") // Предпочтительный язык пользователя
}

class UserEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserEntity>(UserTable)

    var name by UserTable.name
    var city by CityEntity optionalReferencedOn UserTable.cityId
    var avatarUrl by UserTable.avatarUrl
    var photos by UserTable.photos
    var birthDate by UserTable.birthDate
    var email by UserTable.email
    var password by UserTable.password
    var gender by UserTable.gender
    var languages by UserTable.languages
    var locale by UserTable.locale
}
