pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Needed so `./gradlew updateDaemonJvm` can resolve toolchain download URLs for every
    // platform (this repo's gradle-daemon-jvm.properties targets all OS/arch combos, not just
    // this machine's), and so Gradle can auto-provision a JDK toolchain if a required version
    // (e.g. :detekt-rules' jvmToolchain(17)) isn't already installed locally.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Facet Launcher"
include(":app")
include(":detekt-rules")
