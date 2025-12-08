plugins {
    id("buildlogic.kotlin-library-conventions")
    id("buildlogic.kotlin-server-conventions")
    id("buildlogic.kotlin-test-conventions")
}

dependencies {
    implementation(projects.core.auth)
    implementation(libs.mindrot.jbcrypt)
    implementation(libs.fasterxml.jackson.yaml)
}
