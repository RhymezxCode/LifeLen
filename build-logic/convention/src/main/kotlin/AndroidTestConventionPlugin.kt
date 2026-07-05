import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * The JVM-runnable test stack: Robolectric (so Android-framework + Compose tests run under
 * `testDebugUnitTest` without a device), Compose UI test, MockWebServer, coroutines-test and Turbine.
 * Apply to any module that has Android-dependent or Compose UI unit tests. SDK is pinned to 35 via
 * each module's `src/test/resources/robolectric.properties` (Robolectric 4.15 caps at API 35).
 */
class AndroidTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val composeBom = libs.findLibrary("androidx-compose-bom").get()
        val okhttpBom = libs.findLibrary("okhttp-bom").get()
        dependencies {
            add("testImplementation", libs.findLibrary("junit").get())
            add("testImplementation", libs.findLibrary("robolectric").get())
            add("testImplementation", libs.findLibrary("androidx-test-core").get())
            add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
            add("testImplementation", libs.findLibrary("turbine").get())
            add("testImplementation", platform(composeBom))
            add("testImplementation", libs.findLibrary("androidx-compose-ui-test-junit4").get())
            add("testImplementation", libs.findLibrary("androidx-compose-ui-test-manifest").get())
            add("testImplementation", platform(okhttpBom))
            add("testImplementation", libs.findLibrary("okhttp-mockwebserver").get())
        }
    }
}
