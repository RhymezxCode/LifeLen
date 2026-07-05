// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Plugins are declared here (apply false) so module build scripts can apply them by alias
// without re-declaring versions. The `lifelen.*` convention plugins live in `build-logic/`.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}

// Convenience aggregate task so you can run the whole test suite from one place.
tasks.register("allTests") {
    group = "verification"
    description = "Runs all unit tests (across app + core + feature modules) + connected instrumentation tests. " +
        "Instrumentation tests require a running emulator or device."
    dependsOn("test", "connectedAndroidTest")
}

