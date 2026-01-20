package org.patifiner.database

enum class Gender {
    MALE,
    FEMALE,
    OTHER,
    NOT_SPECIFIED
}

enum class LanguageLevel {
    BEGINNER,       // Начинающий
    CONVERSATIONAL, // Разговорный
    FLUENT,         // Свободный
    NATIVE          // Родной
}

data class UserLanguage(
    val languageCode: String,
    val level: LanguageLevel
)
