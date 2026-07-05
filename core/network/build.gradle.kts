plugins {
    alias(libs.plugins.lifelen.android.library)
    alias(libs.plugins.lifelen.android.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.lifelen.android.test)
}

android {
    namespace = "com.lifelen.core.network"
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.mlkit.image.labeling)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(platform(libs.okhttp.bom))
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
}
