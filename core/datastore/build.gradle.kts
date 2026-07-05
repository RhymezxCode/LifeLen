plugins {
    alias(libs.plugins.lifelen.android.library)
    alias(libs.plugins.lifelen.android.hilt)
}

android {
    namespace = "com.lifelen.core.datastore"
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
}
