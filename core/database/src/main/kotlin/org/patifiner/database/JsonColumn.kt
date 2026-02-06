package org.patifiner.database

import com.fasterxml.jackson.core.type.TypeReference
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.jsonb
import org.patifiner.base.PtfJson

inline fun <reified T : Any> Table.jacksonb(name: String): Column<T> =
    jsonb(
        name = name,
        serialize = { PtfJson.mapper.writeValueAsString(it) },
        deserialize = { PtfJson.mapper.readValue(it, object : TypeReference<T>() {}) }
    )
