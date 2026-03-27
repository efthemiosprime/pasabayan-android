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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Pasabayan"
include(":app")
include(":core:designsystem")
include(":core:domain-error")
include(":core:network")

/**
 * AGP's JdkImageTransform resolves jlink using [JAVA_HOME]. Gradle can run on org.gradle.java.home
 * while the daemon still inherits a bad JAVA_HOME from the IDE (e.g. editor JRE without jlink).
 * Point JAVA_HOME at this JVM when the env value is missing or unusable for jlink.
 */
@Suppress("UNCHECKED_CAST")
fun syncJavaHomeForAgpJlink() {
    val runningHome = System.getProperty("java.home") ?: return
    var root = java.io.File(runningHome)
    if (root.name == "jre") {
        root = root.parentFile ?: return
    }
    root = root.canonicalFile
    val jlink = java.io.File(root, "bin/jlink")
    if (!jlink.isFile || !jlink.canExecute()) return

    val want = root.path
    val envHome = System.getenv("JAVA_HOME")
    if (!envHome.isNullOrEmpty()) {
        val envJlink = java.io.File(envHome, "bin/jlink")
        if (envJlink.isFile && envJlink.canExecute()) return
    }

    try {
        val pe = Class.forName("java.lang.ProcessEnvironment")
        val f = pe.getDeclaredField("theEnvironment")
        f.isAccessible = true
        val map = f.get(null) as MutableMap<String, String>
        map["JAVA_HOME"] = want
    } catch (_: Throwable) {
        // JDK layout / module access may block reflection; gradlew should still set JAVA_HOME.
    }
}

syncJavaHomeForAgpJlink()
