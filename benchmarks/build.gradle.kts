import kotlinx.benchmark.gradle.JvmBenchmarkTarget

// Benchmarks live in their own module, not a source set inside :types, so that the JMH and
// kotlinx-benchmark dependencies cannot reach the published artifact. ADR-0001 dependency isolation.
// Nothing here is published, and nothing here gates CI: run it on demand with
//   ./gradlew :benchmarks:benchmark
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinAllOpen)
    alias(libs.plugins.kotlinxBenchmark)
}

kotlin {
    jvmToolchain(17)
}

// JMH generates subclasses of every @State class, so they must not be final.
allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

dependencies {
    implementation(project(":types"))
    implementation(libs.kotlinx.benchmark.runtime)
}

benchmark {
    configurations {
        // A targeted re-run, for when one operation's setup changes and re-measuring the whole
        // suite would cost half an hour to refresh a handful of rows.
        register("spot") {
            include(".*\\.(mod|toString|toDouble)(Kotlin|Jdk)$")
        }
    }
    targets {
        register("main") {
            this as JvmBenchmarkTarget
            jmhVersion = "1.37"
        }
    }
}
