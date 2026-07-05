plugins {
    alias(libs.plugins.lifelen.android.library)
    // Glance builds on the Compose runtime, so its @Composable widget content must be processed
    // by the Kotlin Compose compiler plugin. The compose library convention applies that plugin
    // (and enables buildFeatures.compose) — without it the Glance composables would not compile.
    alias(libs.plugins.lifelen.android.library.compose)
    alias(libs.plugins.lifelen.android.hilt)
}

android {
    namespace = "com.lifelen.feature.widget"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:model"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
}
