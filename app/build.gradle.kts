import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}
val safetyProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")

    if(propertiesFile.exists()) {
        propertiesFile.inputStream().use { load(it) }
  }
}

fun quotedBuildConfigString(value: String): String =
    "\"" + value
.replace(" \\ ", "\\\\")
.replace("\"", "\\\"" )
.replace("\n", "\\n")
.replace("\r", "\\r") + "\""


android {
    namespace = "com.cos229239.team02.oto"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.cos229239.team02.oto"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "NPS_API_KEY",
            quotedBuildConfigString(
                safetyProperties.getProperty("NPS_API_KEY", "")
            )
        )
buildConfigField(
    "String",
    "NWS_USER_AGENT",
    quotedBuildConfigString(
        safetyProperties.getProperty(
            "NWS_USER_AGENT",
            "OTO-Explorer/1.0"
        )
    )
)

    }

    buildTypes {
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
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")

    implementation(libs.google.play.services.location)

    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.okhttp)

    implementation(libs.maplibre.compose) {
        exclude(
            group = "org.maplibre.gl",
            module = "android-sdk"
        )
    }

    implementation(libs.maplibre.android.opengl)

    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}