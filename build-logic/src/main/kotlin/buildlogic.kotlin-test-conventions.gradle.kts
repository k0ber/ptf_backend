plugins {
    id("buildlogic.kotlin-common-conventions")
    `jvm-test-suite`
}

val allureAgent: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

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

    allureAgent(libs.aspectj.weaver)
}

testing {
    suites {
        @Suppress("UnstableApiUsage", "unused")
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()

            targets.all {
                testTask.configure {
                    val allureResultsDir = rootProject.layout.buildDirectory
                        .dir("allure-results").get().asFile

                    doFirst {
                        if (!allureResultsDir.exists()) allureResultsDir.mkdirs()
                    }

                    systemProperty("allure.results.directory", allureResultsDir.absolutePath)
                    systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")

                    val agentFile = allureAgent.files.firstOrNull()
                    if (agentFile != null) {
                        jvmArgs("-javaagent:${agentFile.absolutePath}")
                    }

                    testLogging {
                        events("passed", "skipped", "failed")
                        showExceptions = true
                        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                    }
                }
            }
        }
    }
}