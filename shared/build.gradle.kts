plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    // AGP 9+ vyžaduje pro KMP moduly plugin com.android.kotlin.multiplatform.library;
    // starý com.android.library s kotlin.multiplatform kombinovat nelze.
    androidLibrary {
        namespace = "com.allergytrack.shared"
        compileSdk = 37
        minSdk = 26

        // Bez tohoto by allTests spouštěly jen iOS. Charakterizační testy
        // z fází 5-8 musí běžet na obou platformách a dávat shodné výsledky.
        withHostTestBuilder {}.configure {}
    }

    // iOS targety jsou CANARY — nikdy se nespouštějí, jen musí zkompilovat.
    // Tím kompilátor hlídá, že do commonMain nepropadne nic androidního.
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.datastore.preferences.core)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
