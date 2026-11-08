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
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "WikiReader"
include(":app")
include(":grokipedia")
include(":baselineprofile")
include(":yapchik")
project(":yapchik").projectDir = file("/home/naftali/Tinkering/Libraries/yapchik/yapchik")
