package org.patifiner.database.tables

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object CitiesTable : LongIdTable("cities") {
    val name = varchar("name", 255)
    val country = varchar("country", 100)
    val latitude = double("lat").nullable()
    val longitude = double("lon").nullable()

    // for fast search by title
    init {
        index(isUnique = false, name)
    }
}

class CityEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<CityEntity>(CitiesTable)

    var name by CitiesTable.name
    var country by CitiesTable.country
    var latitude by CitiesTable.latitude
    var longitude by CitiesTable.longitude
}
