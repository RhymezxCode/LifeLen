import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Convention for the single `:app` application module.
 * Applies the Android application + Kotlin plugins and shared SDK/Java/Kotlin settings.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // AGP 9 has built-in Kotlin: applying com.android.application already registers the
        // `kotlin` extension, so we must NOT also apply org.jetbrains.kotlin.android.
        pluginManager.apply("com.android.application")

        extensions.configure<ApplicationExtension> {
            compileSdk = versionInt("compileSdk")
            defaultConfig {
                minSdk = versionInt("minSdk")
                targetSdk = versionInt("targetSdk")
                versionCode = 1
                versionName = "1.0"
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
            testOptions {
                unitTests.isIncludeAndroidResources = true
            }
        }
        // With built-in Kotlin, jvmTarget defaults to compileOptions.targetCompatibility (11).
    }
}
