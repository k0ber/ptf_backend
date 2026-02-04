plugins {
    id("buildlogic.kotlin-library-conventions")
    id("buildlogic.kotlin-server-conventions")
    id("buildlogic.kotlin-database-conventions")
    id("buildlogic.kotlin-test-conventions")
}

dependencies {
    api(projects.core.base)
    api(projects.core.auth)
    api(projects.core.database)
}
