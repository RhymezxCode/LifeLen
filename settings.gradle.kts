pluginManagement {
    // Composite build that provides the `lifelen.*` convention plugins.
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // SimpleStore (RhymezxCode/SimpleStore) is published via JitPack.
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Lifelen"

include(":app")

// Core layer
include(":core:model")
include(":core:common")
include(":core:designsystem")
include(":core:datastore")
include(":core:database")
include(":core:network")
include(":core:search")
include(":core:data")

// Feature layer
include(":feature:scanner")
include(":feature:results")
include(":feature:prices")
include(":feature:history")
include(":feature:settings")
