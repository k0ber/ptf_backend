plugins {
    id("buildlogic.kotlin-library-conventions")
    id("buildlogic.kotlin-server-conventions")
    id("buildlogic.kotlin-database-conventions")
}
dependencies {
    implementation(projects.features.topics)
    implementation(projects.features.user)
    implementation(projects.core.auth)
    implementation(projects.core.base)
    implementation(projects.core.database)
}
