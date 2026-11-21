import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

// #lee keystore.properties (claves del APK firmado)
val propiedadesFirma = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}
// #true si existe la configuracion de firma
val hayFirma = propiedadesFirma.getProperty("storeFile") != null

android {
    namespace = "cl.duoc.tiotecpreventivo"

    // #configuracion de firma con el archivo .jks
    signingConfigs {
        if (hayFirma) {
            create("release") {
                // #ubicacion del .jks
                storeFile = rootProject.file(propiedadesFirma.getProperty("storeFile"))
                storePassword = propiedadesFirma.getProperty("storePassword")
                // #alias de la llave
                keyAlias = propiedadesFirma.getProperty("keyAlias")
                keyPassword = propiedadesFirma.getProperty("keyPassword")
            }
        }
    }

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        // #identificador de la app
        applicationId = "cl.duoc.tiotecpreventivo"
        minSdk = 24
        targetSdk = 37
        // #version de la app
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // #activa el reporte de cobertura (JaCoCo)
            enableUnitTestCoverage = true
            }
        // #build release: firmado y con R8
        release {
            // #firma el APK release con el .jks
            if (hayFirma) signingConfig = signingConfigs.getByName("release")
            optimization {
                // #R8: minimiza y ofusca el codigo
                enable = true
                packageScope = setOf("androidx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

// Room exporta el esquema de cada versión de la base de datos (TT-07).
// Los JSON de app/schemas se suben al repositorio para poder verificar migraciones.
room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    // #Room: base de datos local
    ksp(libs.androidx.room.compiler)
    androidTestImplementation(libs.androidx.room.testing)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // #JUnit para las pruebas unitarias
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    // #MockK para las pruebas unitarias
    testImplementation(libs.mockk)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.window)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // #WorkManager para los recordatorios
    // TT-16: avisos programados que sobreviven a cerrar la app y reiniciar el teléfono.
    implementation(libs.androidx.work.runtime.ktx)

    // #Retrofit para consumir las APIs
    // TT-29: consumo de los microservicios propios con Retrofit (JSON con Gson).
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

}