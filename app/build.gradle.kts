plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "dev.pocketdoc"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.pocketdoc"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("com.github.SUPERCILEX.poi-android:poi:3.17")
    implementation("org.apache.poi:poi-scratchpad:3.17") {
        exclude(group = "org.apache.poi", module = "poi")
    }
    implementation("org.apache.commons:commons-csv:1.14.1")
}
