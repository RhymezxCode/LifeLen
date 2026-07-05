plugins {
    alias(libs.plugins.lifelen.android.library)
    alias(libs.plugins.lifelen.android.hilt)
    alias(libs.plugins.lifelen.android.room)
    alias(libs.plugins.lifelen.android.test)
}

android {
    namespace = "com.lifelen.core.database"
}
