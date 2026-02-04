plugins {
    id("buildlogic.kotlin-test-base-conventions")
}

val aspectjAgent: Configuration by configurations.creating

dependencies {
    api(platform(libs.junit.bom))
    api(libs.junit.jupiter.api)
    api(libs.junit.jupiter.params)
    api(libs.junit.jupiter.engine)

    api(libs.koin.test)
    api(libs.koin.test.junit)

    api(libs.ktor.server.test.host)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.serialization.jackson)

    api(libs.kotlinx.coroutines.test)
    api(libs.jetbrains.kotlin.test)

    api(libs.h2database)

    api(platform(libs.allure.bom))
    api(libs.allure.junit5)

    aspectjAgent(libs.aspectj.weaver) {
        isTransitive = false
    }
}

tasks.withType<Test> {
    val agentFile = aspectjAgent.incoming.artifacts.resolvedArtifacts.map { it.first().file }

    jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf("-javaagent:${agentFile.get().absolutePath}")
    })

    systemProperty("allure.results.directory", layout.buildDirectory.dir("allure-results").get().asFile.absolutePath)
}
