plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Release signing key, read from ~/.gradle/gradle.properties so it never enters the repository.
// Without it, release builds fall back to Android Studio's debug key.
val releaseKeystore: String? = providers.gradleProperty("stepcounter.keystore").orNull

android {
    namespace = "com.ahmadbukhari.stepcounter"
    compileSdk = 37

    defaultConfig {
        // Change this to your own ID before publishing a build. See android/README.md.
        applicationId = "com.ahmadbukhari.stepcounter"
        minSdk = 28
        targetSdk = 37
        versionCode = 2
        versionName = "2.0"
    }

    signingConfigs {
        if (releaseKeystore != null) {
            create("release") {
                storeFile = file(releaseKeystore)
                storePassword = providers.gradleProperty("stepcounter.keystorePassword").get()
                keyAlias = providers.gradleProperty("stepcounter.keyAlias").get()
                keyPassword = providers.gradleProperty("stepcounter.keystorePassword").get()
            }
        }
    }

    buildTypes {
        release {
            // Shrinks the APK from about 33 MB to a few MB.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.health.connect)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.datastore.preferences)
}
