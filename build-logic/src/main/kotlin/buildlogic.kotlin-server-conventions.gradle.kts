plugins {
    id("buildlogic.kotlin-common-conventions")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.status.pages)

    implementation(libs.logback.classic)

    implementation(libs.ktor.serialization.jackson)
    implementation(libs.fasterxml.jackson.module)
    implementation(libs.fasterxml.jackson.datatype)
}
