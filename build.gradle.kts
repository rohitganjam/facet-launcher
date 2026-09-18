plugins {
    id("com.android.application") version "9.4.0" apply false
    // Pins the Kotlin compiler version explicitly rather than relying on AGP 9's built-in default.
    id("org.jetbrains.kotlin.android") version "2.3.10" apply false
    // Plain-JVM Kotlin, for :detekt-rules only — a custom Detekt rule has to be a separate,
    // pre-compiled module (Detekt loads it as a plugin jar), so it can't be an Android module itself.
    id("org.jetbrains.kotlin.jvm") version "2.3.10" apply false
    // 2.0.0-alpha.6, not the 1.23.x stable line — 1.23.8's bundled Kotlin-compiler-adjacent code
    // crashes parsing this machine's JDK 25 version string ("25.0.3"); 2.0's alpha line supports
    // it. Revisit pinning to a stable release once 2.0 ships one.
    id("dev.detekt") version "2.0.0-alpha.6" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.10" apply false
    id("com.google.devtools.ksp") version "2.3.10" apply false
    id("com.google.dagger.hilt.android") version "2.60.1" apply false
}
