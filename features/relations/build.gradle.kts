plugins {
    id("buildlogic.kotlin-library-conventions")
    id("buildlogic.kotlin-server-conventions")
    id("buildlogic.kotlin-database-conventions")
}

dependencies {
    implementation(projects.core.base)
    implementation(projects.core.auth)
    implementation(projects.core.database)

    implementation(projects.features.user) // common dto module instead?
}
