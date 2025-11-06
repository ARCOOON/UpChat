pluginManagement {
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

    plugins {
        id("org.jetbrains.kotlin.android") version "2.2.21"
        id("com.google.devtools.ksp") version "2.2.21-1.0.27"
        id("com.android.application") version "8.13.0"
        id("com.android.library") version "8.13.0"
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "UpChat"

include(":app")
include(":ui")
include(":core")
include(":data")
