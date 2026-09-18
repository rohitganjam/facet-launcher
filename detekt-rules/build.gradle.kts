plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(17)
}

// Must match the detekt-gradle-plugin version applied in the root build.gradle.kts — detekt-api
// is not binary-compatible across minor versions (and 2.0's alpha line is actively breaking API,
// e.g. no more CodeSmell/Debt, Rule/RuleSetProvider construction reshaped — see
// ComposeHardcodedTextRule's doc comment), so a mismatch here shows up as
// NoSuchMethodError/ClassNotFoundException only when `./gradlew detekt` actually runs this
// module's rule, not at compile time. Group moved from io.gitlab.arturbosch.detekt to dev.detekt
// as of 2.0.
val detektVersion = "2.0.0-alpha.6"

dependencies {
    compileOnly("dev.detekt:detekt-api:$detektVersion")

    testImplementation("dev.detekt:detekt-test:$detektVersion")
    testImplementation("junit:junit:4.13.2")
}
