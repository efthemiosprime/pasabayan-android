import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    // Must be applied last — processes app/google-services.json (Firebase + Google OAuth client IDs)
    alias(libs.plugins.google.services)
}

// Google Maps API key — read from local.properties (`MAPS_API_KEY=...`) and
// injected as a manifest placeholder. Empty string is safe: the map will render
// a watermarked "API key required" tile but the app still builds and runs.
val mapsApiKey: String = run {
    val props = Properties()
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { props.load(it) }
    }
    props.getProperty("MAPS_API_KEY", "")
}

android {
    namespace = "com.efthemiosprime.pasabayan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.efthemiosprime.pasabayan"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Injected into AndroidManifest as `com.google.android.geo.API_KEY`.
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // Exposes java.time.*, j.u.stream.*, j.u.function.* on API < 26 so the
        // many `Instant.parse` / `Instant.compareTo` call sites in trip + match
        // ViewModels stop crashing on minSdk=25 devices. Without this, lint
        // surfaces 73 NewApi errors targeting java.time APIs.
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.all { test ->
            // Default Gradle test-worker heap (Xmx512m) is not enough for the
            // full :app: suite — Hilt/Compose/Stripe deps push heap usage past
            // the limit and the worker OOMs (sometimes silently — appears as a
            // hang while the JVM thrashes GC).
            test.maxHeapSize = "2g"
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:domain-error"))
    implementation(project(":core:network"))
    implementation(project(":core:session"))

    // [Json] + Retrofit [Response] for Hilt-injected repositories that call [ApiErrorMapper] (e.g. city onboarding).
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)

    // XML theme (splash / window) — Material3 DayNight matches Compose MaterialTheme parity
    implementation(libs.google.material)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Firebase (FCM) — BOM aligns messaging with google-services.json
    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-messaging")

    // Google Sign-In + Credential Manager–related Play Services (parity with pasabayan-android-develop)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.identity)
    implementation(libs.play.services.location)

    // Facebook Login — meta-data + activities in AndroidManifest
    implementation(libs.facebook.login)

    // Stripe — PaymentSheet, SetupIntent, Google Pay
    implementation(libs.stripe.android)

    // Coil 3 — used by feature composables (e.g. ShipperServiceReceiptCard); core:designsystem
    // also depends on it but as `implementation` so the dependency isn't transitive.
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Chrome Custom Tabs — Stripe Connect onboarding / dashboard
    implementation(libs.androidx.browser)

    // Google Maps — interactive map for service-request store / delivery pickers
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.retrofit.kotlinx.serialization)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
