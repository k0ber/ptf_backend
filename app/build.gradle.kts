plugins {
    id("buildlogic.kotlin-application-conventions")
    id("buildlogic.kotlin-server-conventions")
    id("buildlogic.kotlin-database-conventions")
    id("buildlogic.kotlin-test-base-conventions")
}

dependencies {
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.double.receive)

    implementation(libs.koin.logger)
    implementation(libs.exposed.core)

    implementation(projects.core.base)
    implementation(projects.core.auth)
    implementation(projects.core.database)

    implementation(projects.features.check)
    implementation(projects.features.user)
    implementation(projects.features.topics)
    implementation(projects.features.search)
    implementation(projects.features.upload)
    implementation(projects.features.geo)
    implementation(projects.features.events)
    implementation(projects.features.relations)

    testImplementation(projects.core.testing)
}

application {
    mainClass = "org.patifiner.app.AppKt"
}
