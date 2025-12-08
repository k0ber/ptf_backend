plugins {
    alias(libs.plugins.kotlin.jvm)

    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
//    id("org.jetbrains.kotlin.jvm")
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

//dependencies {
//    constraints {
        // Define dependency versions as constraints
        // implementation("org.apache.commons:commons-text:1.13.0")
//    }
//}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
