import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    alias(libs.plugins.multiplatformLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply  false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
}

rootProject.plugins.withType<YarnPlugin> {
    rootProject.the<YarnRootExtension>().lockFileDirectory =
        project.rootDir.resolve("gradle/kotlin-js-store")
}
