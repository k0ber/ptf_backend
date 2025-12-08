plugins {
    id("buildlogic.kotlin-common-conventions")
}

dependencies {
    implementation(libs.exposed.core)   // Основной модуль Exposed
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.exposed.dao)    // Exposed для DAO
    implementation(libs.exposed.jdbc)   // Exposed для работы с JDBC
    implementation(libs.hikariCP)       // Для управления соединениями с БД
    implementation(libs.postgresql)     // Для подключения к PostgreSQL
}
