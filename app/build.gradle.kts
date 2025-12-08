plugins {
    id("buildlogic.kotlin-application-conventions")
    id("buildlogic.kotlin-server-conventions")
}

dependencies {
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)

    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.double.receive)

    implementation(libs.koin.logger)

    implementation(libs.exposed.core)

    implementation(projects.core.auth)
    implementation(projects.core.auth)
    implementation(projects.features.check)

    implementation(projects.features.profile)
    implementation(projects.features.search)
    implementation(projects.features.upload)
}

application {
    mainClass = "org.patifiner.app.AppKt"
}
