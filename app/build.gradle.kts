import java.util.Properties

plugins {
    alias(libs.plugins.lifelen.android.application)
    alias(libs.plugins.lifelen.android.application.compose)
    alias(libs.plugins.lifelen.android.hilt)
}

// Build-time API-key defaults, read from a gitignored secrets.properties at the repo root.
// Users can also enter keys at runtime in the Settings screen (stored in DataStore).
val secrets = Properties().apply {
    val file = rootProject.file("secrets.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun secretField(key: String): String = "\"${secrets.getProperty(key).orEmpty()}\""

android {
    namespace = "com.lifelen"

    defaultConfig {
        applicationId = "com.lifelen"
        buildConfigField("String", "DASHSCOPE_API_KEY", secretField("DASHSCOPE_API_KEY"))
        buildConfigField("String", "SEARCH_API_KEY", secretField("SEARCH_API_KEY"))
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:data"))

    implementation(project(":feature:scanner"))
    implementation(project(":feature:results"))
    implementation(project(":feature:prices"))
    implementation(project(":feature:history"))
    implementation(project(":feature:settings"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
