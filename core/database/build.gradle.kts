plugins {
    id("buildlogic.kotlin-library-conventions")
    id("buildlogic.kotlin-server-conventions")
    id("buildlogic.kotlin-database-conventions")
}

dependencies {
    implementation(libs.fasterxml.jackson.yaml)
    implementation(libs.exposed.json)
}
