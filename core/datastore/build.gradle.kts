plugins {
    alias(libs.plugins.lifelen.android.library)
    alias(libs.plugins.lifelen.android.hilt)
}

android {
    namespace = "com.lifelen.core.datastore"

    // SimpleStore ships `inline fun getType()` compiled to JVM target 17, so this module must
    // also target 17 to inline it. Other modules keep 11 (they never inline SimpleStore code).
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // SimpleStore wraps DataStore/SharedPreferences behind a single builder API.
    implementation(libs.simplestore)
}
