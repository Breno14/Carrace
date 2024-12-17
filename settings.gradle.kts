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
<<<<<<< HEAD
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) 
=======
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
>>>>>>> 98c07a1 (Projeto car race)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CarRace"
include(":app")
include(":simulate")

