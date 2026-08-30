plugins {
    id("com.android.application") version "9.2.1" apply false
    // Pins the Kotlin compiler version explicitly rather than relying on AGP 9's built-in default.
    id("org.jetbrains.kotlin.android") version "2.3.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10" apply false
    id("com.google.devtools.ksp") version "2.3.10" apply false
    id("com.google.dagger.hilt.android") version "2.60.1" apply false
}
