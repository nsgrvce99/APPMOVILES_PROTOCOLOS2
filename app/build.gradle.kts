plugins {
    jacoco
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val gatewayAcademico = providers.gradleProperty("induztek.baseUrl").orElse("http://10.0.2.2:8080/")
val gatewayProduccion = providers.gradleProperty("induztek.releaseBaseUrl").orElse("")

android {
    namespace = "cl.duocuc.miregistro"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        buildConfigField("boolean", "RED_LOCAL",
            providers.gradleProperty("induztek.redLocal").map { it == "true" }.orElse(false).get().toString())
        buildConfigField("boolean", "DATOS_LOCALES",
            providers.gradleProperty("induztek.datosLocales").map { it == "true" }.orElse(false).get().toString())
        applicationId = "cl.duocuc.miregistro"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val firma = listOf("ANDROID_KEYSTORE", "ANDROID_STORE_PASSWORD", "ANDROID_KEY_ALIAS", "ANDROID_KEY_PASSWORD")
        .associateWith { providers.environmentVariable(it).orNull }
    val firmaRelease = if (firma.values.all { !it.isNullOrBlank() }) {
        signingConfigs.create("academica") {
            storeFile = rootProject.file(firma.getValue("ANDROID_KEYSTORE")!!)
            storePassword = firma.getValue("ANDROID_STORE_PASSWORD")
            keyAlias = firma.getValue("ANDROID_KEY_ALIAS")
            keyPassword = firma.getValue("ANDROID_KEY_PASSWORD")
        }
    } else null

    val pruebaFirmada = providers.gradleProperty("induztek.pruebaFirmada").orNull == "true"
    require(!pruebaFirmada || firmaRelease != null) {
        "Configura ANDROID_KEYSTORE, ANDROID_STORE_PASSWORD, ANDROID_KEY_ALIAS y ANDROID_KEY_PASSWORD para instrumentar el APK firmado."
    }
    testBuildType = if (pruebaFirmada) "release" else "debug"

    flavorDimensions += "distribucion"
    productFlavors {
        create("academico") {
            dimension = "distribucion"
            signingConfig = firmaRelease
            buildConfigField("String", "INDUZTEK_BASE_URL", gatewayAcademico.get().quoted())
            buildConfigField("boolean", "PERMITE_HTTP_GATEWAY", "true")
        }
        create("produccion") {
            dimension = "distribucion"
            buildConfigField("String", "INDUZTEK_BASE_URL", gatewayProduccion.get().quoted())
            buildConfigField("boolean", "PERMITE_HTTP_GATEWAY", "false")
        }
    }
    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    //Librerias nuevas
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")

    // NUEVO (Bloque 1): navegación entre pantallas (NavHost, NavController)
    implementation("androidx.navigation:navigation-compose:2.10.2")

    implementation("androidx.compose.material:material-icons-extended")
    implementation("io.coil-kt:coil-compose:2.6.0")

}
fun String.quoted(): String = "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

jacoco {
    toolVersion = "0.8.14"
}

tasks.withType<Test>().configureEach {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        includes = listOf("cl.duocuc.miregistro.*")
    }
}

tasks.register<JacocoReport>("reporteCoberturaDebug") {
    dependsOn("testAcademicoDebugUnitTest")
    executionData(layout.buildDirectory.file("outputs/unit_test_code_coverage/academicoDebugUnitTest/testAcademicoDebugUnitTest.exec"))
    sourceDirectories.setFrom(files("src/main/java"))
    classDirectories.setFrom(fileTree(layout.buildDirectory.dir(
        "intermediates/built_in_kotlinc/academicoDebug/compileAcademicoDebugKotlin/classes"
    ).get().asFile) {
        include("**/*.class")
        exclude("**/BuildConfig.class", "**/R.class", "**/R\$*.class")
    })
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}
