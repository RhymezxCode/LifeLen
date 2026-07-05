plugins {
    alias(libs.plugins.lifelen.android.library)
    alias(libs.plugins.lifelen.android.hilt)
}

android {
    namespace = "com.lifelen.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
