import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.novatech.terratech"
    compileSdk { version = release(37) }

    defaultConfig {
        applicationId = "com.novatech.terratech"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        if (
            providers
                .gradleProperty("android.testInstrumentationRunnerArguments.tb1BackendJourney")
                .orNull != "true"
        ) {
            testInstrumentationRunnerArguments["notClass"] =
                "com.novatech.terratech.BackendJourneyTest"
        }
    }

    sourceSets.getByName("androidTest").assets.srcDir("$projectDir/schemas")
    buildFeatures { buildConfig = true }
    defaultConfig {
        val apiUrl =
            providers
                .gradleProperty("TERRATECH_API_URL")
                .orElse("https://terratech-api.lucemz.com/")
                .get()
        require(apiUrl.endsWith("/")) { "TERRATECH_API_URL must end with /" }
        buildConfigField("String", "API_URL", "\"$apiUrl\"")
        val local =
            Properties().apply {
                rootProject
                    .file("local.properties")
                    .takeIf { it.exists() }
                    ?.inputStream()
                    ?.use { load(it) }
            }
        val mapsKey =
            providers
                .environmentVariable("TERRATECH_MAPS_API_KEY")
                .orElse(local.getProperty("MAPS_API_KEY", ""))
                .get()
        manifestPlaceholders["MAPS_API_KEY"] = ""
        buildConfigField("boolean", "MAPS_CONFIGURED", "false")
        buildTypes.getByName("debug") {
            manifestPlaceholders["MAPS_API_KEY"] = mapsKey
            buildConfigField("boolean", "MAPS_CONFIGURED", mapsKey.isNotBlank().toString())
        }
    }
    buildTypes { release { optimization { enable = false } } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
}

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

dependencies {
    implementation("com.google.maps.android:maps-compose:9.0.0")
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.datastore.preferences)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockwebserver)
    androidTestImplementation(libs.mockwebserver)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4-accessibility")

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
