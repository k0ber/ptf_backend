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
                }
            }
        }
    }
}
