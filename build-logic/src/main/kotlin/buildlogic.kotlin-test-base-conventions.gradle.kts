plugins {
    id("buildlogic.kotlin-common-conventions")
    `jvm-test-suite`
}

testing {
    suites {
        @Suppress("UnstableApiUsage", "unused")
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()

            targets.all {
                testTask.configure {
                    failOnNoDiscoveredTests = false

                    testLogging {
                        events("passed", "skipped", "failed", "standardOut", "standardError")
                        showExceptions = true
                        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                        showCauses = true
                        showStackTraces = true
                    }
                }
            }
        }
    }
}
