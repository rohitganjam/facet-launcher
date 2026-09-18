import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("dev.detekt")
}

// Release signing — kept out of git entirely (see .gitignore). `keystore.properties` holds the
// real path/passwords; its absence (a fresh checkout, CI without secrets) only breaks a release
// build, never debug, so this stays null rather than error()-ing at configuration time.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) keystorePropertiesFile.inputStream().use { load(it) }
}

android {
    namespace = "com.facetlauncher.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.facetlauncher.app"
        minSdk = 31
        targetSdk = 36
        versionCode = 10
        versionName = "0.1.9"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            // This app has no native code of its own, but the release APK still bundles a
            // handful of transitive .so libs (AndroidX Graphics' libandroidx.graphics.path.so,
            // DataStore's libdatastore_shared_counter.so) — without this, a native crash in
            // either would show up in Play Console's Android vitals as raw unsymbolicated hex
            // addresses instead of a real stack trace. AGP bundles the resulting native debug
            // symbol table directly in the release AAB (bundleRelease) automatically; no
            // separate upload to Play Console is needed.
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests {
            // Robolectric JVM unit tests need this to resolve real compiled resources (e.g.
            // context.getString(R.string.x)) — without it, any resource lookup throws
            // Resources$NotFoundException at runtime even though the resource genuinely exists
            // and the code compiles fine. Never needed until SystemSettingsRepositoryTest became
            // the first test to call context.getString() on an app string resource.
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
        }
        getByName("test") {
            java.srcDirs("src/test/kotlin")
        }
        getByName("androidTest") {
            java.srcDirs("src/androidTest/kotlin")
            // Exported Room schema JSONs (room.schemaLocation below) — MigrationTestHelper reads
            // them via Context.getAssets() at <databaseClass.canonicalName>/<version>.json, which
            // already matches this folder's own layout. AGP only merges assets for real
            // (non-unit-test) variants, so this must live on androidTest, not test — see
            // FacetDatabaseMigrationTest.
            assets.srcDirs("$projectDir/schemas")
        }
    }
}

dependencies {
    detektPlugins(project(":detekt-rules"))
    // Third-party Compose best-practice rules (Modifier placement/ordering, state hoisting,
    // remember misuse, etc.) — mechanizes several conventions CLAUDE.md already asks for by hand.
    // Pinned to the exact version built against this project's detekt-core (2.0.0-alpha.6, see
    // its POM) rather than "latest", since compose-rules tracks detekt's API closely.
    detektPlugins("io.nlopez.compose.rules:detekt:0.6.6")

    implementation(platform("androidx.compose:compose-bom:2026.01.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")

    implementation("com.google.dagger:hilt-android:2.60.1")
    ksp("com.google.dagger:hilt-compiler:2.60.1")

    implementation("androidx.navigation:navigation-compose:2.9.6")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")

    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

    // 1.2.1, not 1.2.0: 1.2.0's libdatastore_shared_counter.so has a GNU_RELRO segment that
    // doesn't end on a 16 KB boundary (Play Console flags this as a crash risk on 16 KB-page
    // devices) — confirmed by pulling both .so files from Maven and diffing their ELF program
    // headers directly, since the 1.2.1 release notes don't call this out in prose. 1.2.1's
    // binary is a real rebuild (new .relro_padding section, RELRO end lands exactly on 0xC000)
    // and isn't just a relabeled 1.2.0.
    implementation("androidx.datastore:datastore-preferences:1.2.1")

    // F14 Backup & Restore — the exported/imported JSON snapshot's own model (data/model/BackupBundle.kt).
    // Pinned at exactly 1.8.1, not left to float: androidx.room:room-testing:2.8.4 (used by
    // FacetDatabaseMigrationTest) needs 1.8.1's kotlinx-serialization, while
    // androidx.navigation:navigation-common:2.9.6 only ever requests 1.7.3 — without this real
    // main-classpath dependency, AGP's automatic main/androidTest consistent-resolution settles
    // on 1.7.3 and room-testing's precompiled schema-bundle classes crash with AbstractMethodError
    // (a GeneratedSerializer method 1.7.3 doesn't implement). Declaring this here, at the version
    // Room actually needs, is what makes that resolution land on 1.8.1 without a manual
    // resolutionStrategy.force() override.
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("androidx.test:core:1.7.0")

    androidTestImplementation(platform("androidx.compose:compose-bom:2026.01.01"))
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.7.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.navigation:navigation-testing:2.9.6")
    androidTestImplementation("org.mockito:mockito-android:5.23.0")
    androidTestImplementation("androidx.room:room-testing:2.8.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestUtil("androidx.test:orchestrator:1.5.1")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

detekt {
    // Only our own :detekt-rules "facet" rule set runs — detekt's bundled default rule sets
    // (style/complexity/etc.) were never opted into and would otherwise flood the build with
    // ~1900 pre-existing, unrelated findings (MaxLineLength, ReturnCount, ...) that have nothing
    // to do with the language-translation migration this module exists to gate.
    disableDefaultRuleSets = true
    baseline = file("detekt-baseline.xml")
    source.setFrom(files("src/main/kotlin"))
    config.setFrom(files("detekt.yml"))
}

// detekt's bundled Kotlin-compiler-adjacent code predates this machine's JDK default and doesn't
// recognize its version string as a --jvm-target value — pin explicitly to match the rest of the
// project.
tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
    jvmTarget = "17"
}
tasks.withType<dev.detekt.gradle.DetektCreateBaselineTask>().configureEach {
    jvmTarget = "17"
}
