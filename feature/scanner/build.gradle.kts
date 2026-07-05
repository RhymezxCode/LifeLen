plugins {
    alias(libs.plugins.lifelen.android.feature)
}

android {
    namespace = "com.lifelen.feature.scanner"
}

dependencies {
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.image.labeling)
}
