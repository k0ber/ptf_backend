plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "backend"

includeBuild("build-logic")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "app",

    ":features:check",
    ":features:search",
    ":features:topics",
    ":features:upload",
    ":features:user",

    ":core:base",
    ":core:auth",
    ":core:database",
    ":core:postgres",
)
