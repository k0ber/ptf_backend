plugins {
    id("buildlogic.kotlin-library-conventions")
    id("buildlogic.kotlin-server-conventions")
    id("buildlogic.kotlin-database-conventions")
}
dependencies {
    implementation(projects.features.profile)
    implementation(projects.core.auth)
}
