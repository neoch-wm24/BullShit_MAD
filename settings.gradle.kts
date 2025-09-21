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
        maven(url = "https://jitpack.io") // ✅ 插件依赖也能用 JitPack
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io") // ✅ 这里给依赖库用 JitPack
    }
}

rootProject.name = "Logistic-Management-Application"

include(":app")
include(":core-ui")
include(":core-resources")
include(":main-screen")
include(":user-management")
include(":order-management")
include(":delivery-and-transportation-management")
include(":warehouse-management")
include(":core-data")