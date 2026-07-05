plugins {
    alias(libs.plugins.lifelen.android.library)
    alias(libs.plugins.lifelen.android.library.compose)
}

android {
    namespace = "com.lifelen.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material.icons.extended)
    api(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
