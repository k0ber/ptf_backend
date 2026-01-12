plugins {
    id("buildlogic.kotlin-library-conventions")
    id("buildlogic.kotlin-server-conventions")
    id("buildlogic.kotlin-test-conventions")
    id("buildlogic.kotlin-database-conventions")
}

dependencies {
    implementation(projects.core.auth)
    implementation(projects.core.base)
    implementation(projects.core.database)
    implementation(projects.core.postgres)
    implementation(libs.fasterxml.jackson.yaml)
}
