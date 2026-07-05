plugins {
    alias(libs.plugins.lifelen.android.library)
    alias(libs.plugins.lifelen.android.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.lifelen.android.test)
}

android {
    namespace = "com.lifelen.core.data"
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:search"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))

    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
