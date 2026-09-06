plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.lumenlauncher.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lumenlauncher.app"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
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
            // LumenDatabaseMigrationTest.
            assets.srcDirs("$projectDir/schemas")
        }
    }
}

dependencies {
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

    implementation("androidx.datastore:datastore-preferences:1.2.0")

    // F14 Backup & Restore — the exported/imported JSON snapshot's own model (data/model/BackupBundle.kt).
    // Pinned at exactly 1.8.1, not left to float: androidx.room:room-testing:2.8.4 (used by
    // LumenDatabaseMigrationTest) needs 1.8.1's kotlinx-serialization, while
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
