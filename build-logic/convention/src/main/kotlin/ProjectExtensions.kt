import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

// NOTE: these MUST be `internal`. If `Project.libs` were public it would leak onto every
// module's build-script classpath and SHADOW Gradle's generated type-safe `libs` accessor,
// making `libs.androidx.*` unresolvable in build.gradle.kts files.

/** Accessor for the shared `libs` version catalog from inside convention plugins. */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/** Reads an integer version (e.g. compileSdk/minSdk/targetSdk) from the version catalog. */
internal fun Project.versionInt(alias: String): Int =
    libs.findVersion(alias).get().requiredVersion.toInt()

/** Aligns the Kotlin (pure JVM) target with the module's Java compatibility level. */
internal fun Project.configureKotlinJvmTarget() {
    extensions.configure<KotlinJvmProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}

/** Adds the Compose BOM and the baseline Compose UI dependencies used by every Compose module. */
internal fun Project.addComposeDependencies() {
    val bom = libs.findLibrary("androidx-compose-bom").get()
    dependencies {
        add("implementation", platform(bom))
        add("implementation", libs.findLibrary("androidx-compose-ui").get())
        add("implementation", libs.findLibrary("androidx-compose-ui-graphics").get())
        add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
        add("implementation", libs.findLibrary("androidx-compose-material3").get())
        add("androidTestImplementation", platform(bom))
        add("androidTestImplementation", libs.findLibrary("androidx-compose-ui-test-junit4").get())
        add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
        add("debugImplementation", libs.findLibrary("androidx-compose-ui-test-manifest").get())
    }
}
