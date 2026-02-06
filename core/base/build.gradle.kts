plugins {
    id("buildlogic.kotlin-library-conventions")
    id("buildlogic.kotlin-server-conventions")
}

dependencies {
    implementation(libs.ktor.server.content.negotiation)
}
