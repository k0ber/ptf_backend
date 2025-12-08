plugins {
    id("buildlogic.kotlin-library-conventions")
    id("buildlogic.kotlin-server-conventions")
}

dependencies {
    api(libs.ktor.server.auth.jwt.jvm)
    api(libs.ktor.server.auth.jvm)
}
