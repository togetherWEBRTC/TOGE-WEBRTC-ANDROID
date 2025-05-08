import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.junit5)
}

android {
    namespace = libs.versions.app.application.id.get()
    compileSdk = libs.versions.app.target.sdk.get().toInt()

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            all { testTask ->
                testTask.useJUnitPlatform()
            }
        }
    }

    defaultConfig {
        applicationId = libs.versions.app.application.id.get()
        minSdk = libs.versions.app.min.sdk.get().toInt()
        targetSdk = libs.versions.app.target.sdk.get().toInt()
        versionCode = libs.versions.app.version.code.get().toInt()
        versionName = libs.versions.app.version.name.get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "TURN_SERVER_URL",
            "\"${getLocalProperties("LOCAL_TURN_SERVER_URL")}\""
        )
        buildConfigField(
            "String",
            "TURN_SERVER_USERNAME",
            "\"${getLocalProperties("LOCAL_TURN_SERVER_USERNAME")}\""
        )
        buildConfigField(
            "String",
            "TURN_SERVER_PASSWORD",
            "\"${getLocalProperties("LOCAL_TURN_SERVER_PASSWORD")}\""
        )
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "TOGE_WRTC_DEV")
            buildConfigField("String", "LOCAL_PREF", "\"toge_pref\"")
            buildConfigField("String", "API_URL", "\"${getLocalProperties("LOCAL_API_URL")}\"")
            buildConfigField("String", "RES_URL", "\"${getLocalProperties("LOCAL_API_URL")}\"")
            buildConfigField("String", "WEBSOCKET_URL", "\"${getLocalProperties("LOCAL_WEBSOCK_URL")}\"")
        }

        release {
            resValue("string", "app_name", "TOGE_WRTC")
            isMinifyEnabled = false
            buildConfigField("String", "LOCAL_PREF", "\"toge_pref\"")
            buildConfigField("String", "API_URL", "\"${getLocalProperties("LOCAL_API_URL")}\"")
            buildConfigField("String", "RES_URL", "\"${getLocalProperties("LOCAL_API_URL")}\"")
            buildConfigField("String", "WEBSOCKET_URL", "\"${getLocalProperties("LOCAL_WEBSOCK_URL")}\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
}

fun Project.getLocalProperties(propertyKey: String): String =
    gradleLocalProperties(rootDir, providers).getProperty(propertyKey) ?: ""


dependencies {

    //  Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    //  Ui
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.constraintlayout.compose)

    //  Coil
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)

    //  Lottie
    implementation(libs.lottie.compose)

    //  Accompanist
    implementation(libs.accompanist.permissions)

    //  AndroidX Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation)
    implementation(libs.hilt.navigation.compose)

    //  Navigation
    implementation(libs.androidx.navigation.compose)

    //  Kotlin Serialization
    implementation(libs.kotlin.serialization)

    //  Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlin.converter)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    //  Timber
//    implementation(libs.timber)

    //  DataStore
    implementation(libs.datastore)

    //  Socketio
    implementation("io.socket:socket.io-client:2.0.0") {
        exclude(group = "org.json", module = "json")
    }

    //  WebRTC
    implementation(files("$rootDir/libs/libwebrtc.jar"))

    //  Test
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.agent)
    testImplementation(libs.kotlinx.coroutines.test)

}
