import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention for every `:core:*` / `:feature:*` Android library module.
 * Applies the Android library + Kotlin plugins and shared SDK/Java/Kotlin settings.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // AGP 9 has built-in Kotlin: applying com.android.library already registers the
        // `kotlin` extension, so we must NOT also apply org.jetbrains.kotlin.android.
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            compileSdk = versionInt("compileSdk")
            defaultConfig {
                minSdk = versionInt("minSdk")
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

        dependencies {
            add("implementation", libs.findLibrary("kotlinx-coroutines-android").get())
            add("testImplementation", libs.findLibrary("junit").get())
        }
    }
}
