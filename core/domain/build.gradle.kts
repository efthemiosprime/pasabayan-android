plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":core:domain-error"))
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
}
