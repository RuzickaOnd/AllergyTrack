import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

android {
    namespace = "com.allergytrack.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.allergytrack.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1000302
        versionName = "1.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String", "SUPABASE_URL",
            "\"${localProperties["SUPABASE_URL"] ?: ""}\""
        )
        buildConfigField(
            "String", "SUPABASE_KEY",
            "\"${localProperties["SUPABASE_KEY"] ?: ""}\""
        )
    }

    buildTypes {
        debug {
            buildConfigField(
                "String", "REVENUECAT_API_KEY",
                "\"${localProperties["REVENUECAT_TEST_API_KEY"] ?: ""}\""
            )
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
        }
        release {
            buildConfigField(
                "String", "REVENUECAT_API_KEY",
                "\"${localProperties["REVENUECAT_API_KEY"] ?: ""}\""
            )
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
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
        buildConfig = true
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.work.runtime.ktx)

    // Koin — DI náhrada za Hilt (KMP migrace, fáze 2)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.workmanager)
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.datastore.preferences)
    implementation(libs.coil.compose)
    implementation(libs.play.services.location)
    implementation(libs.vico.compose)
    implementation(libs.vico.m3)
    implementation(libs.mlkit.image.labeling)
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.mlkit.genai.prompt)
    implementation(libs.androidx.material3.windowsizeclass)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    implementation(libs.play.app.update)
    implementation(libs.play.app.update.ktx)

    // RevenueCat
    implementation(libs.revenuecat.purchases)
    implementation(libs.revenuecat.purchases.ui)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.testing.ui.manifest)
    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.coroutines)
    testImplementation(platform(libs.koin.bom))
    testImplementation(libs.koin.test)
    // Jen pro KoinModuleGraphTest — verify() čte konstruktory reflexí
    // a potřebuje vidět typ HttpClient. Testy běží na JVM, do APK to nejde,
    // takže se to netýká dexování (Ktor 3.2+ tam má problém, viz níže).
    testImplementation(libs.ktor.client.core)
    androidTestImplementation(libs.testing.androidx.junit)
    androidTestImplementation(libs.testing.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.testing.ui.junit4)
}
