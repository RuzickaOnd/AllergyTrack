plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
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
    //
    // iosX64 (simulátor na Intel Macích) chybí záměrně: androidx.sqlite
    // :sqlite-bundled:2.7.0 pro něj nepublikuje variantu. Není to volba —
    // s Room KMP ten target prostě nejde sestavit. Vývoj běží na Apple
    // Siliconu, kde simulátor pokrývá iosSimulatorArm64.
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
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
            // api, ne implementation — generovaná třída Res musí být
            // viditelná z :app, kde zatím zůstává Compose UI.
            api(compose.runtime)
            api(compose.components.resources)
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

room {
    schemaDirectory("$projectDir/schemas")
}

/**
 * publicResClass = true — generovaná třída Res musí být viditelná z :app,
 * protože Compose UI tam zatím zůstává.
 */
compose.resources {
    publicResClass = true
    packageOfResClass = "com.allergytrack.shared.resources"
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}
