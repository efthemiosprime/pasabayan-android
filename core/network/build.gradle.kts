plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.efthemiosprime.pasabayan.core.network"
    compileSdk = 35

    defaultConfig {
        minSdk = 25
        consumerProguardFiles("consumer-rules.pro")
        // Production API — matches iOS APIConfiguration (release / USE_PRODUCTION_API).
        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"https://api.pasabayan.com/api\"",
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    testImplementation(libs.junit)

    implementation(project(":core:domain"))
    implementation(project(":core:domain-error"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
}
