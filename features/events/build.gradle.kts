plugins {
    id("buildlogic.kotlin-library-conventions")
    id("buildlogic.kotlin-server-conventions")
    id("buildlogic.kotlin-test-conventions")
}

dependencies {
    implementation(projects.core.auth)
    implementation(projects.core.base)
}
