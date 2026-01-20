package org.patifiner.database

import kotlinx.serialization.json.Json

object DatabaseConfig {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
}
