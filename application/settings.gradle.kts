pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()  // 👈 This is where the library lives. It MUST be here.
        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "ServiceConnect" // Ensure this matches your project name
include(":app")