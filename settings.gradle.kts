plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "backend"

includeBuild("build-logic")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "app",
    ":core:auth",
    ":features:check",
    ":features:profile",
    ":features:search",
    ":features:upload",

)
