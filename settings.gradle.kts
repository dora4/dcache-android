pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            setUrl("https://maven.aliyun.com/repository/jcenter")
        }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.github.dcendents") {
                useModule("com.github.dcendents:android-maven-gradle-plugin:1.5")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        mavenLocal()
        google()
        maven {
            setUrl("https://jitpack.io")
        }
        maven {
            setUrl("https://maven.aliyun.com/repository/jcenter")
        }
    }
}

include(":dcache")
rootProject.name = "dcache"
