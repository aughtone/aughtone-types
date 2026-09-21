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
        // A targeted re-run, for when one operation changes and re-measuring the whole suite would
        // cost half an hour to refresh a handful of rows. Pass a regular expression matched against
        // the fully qualified benchmark name:
        //
        //   ./gradlew :benchmarks:spotBenchmark -Pbenchmark.spot='.*\.(toString|parse)(Kotlin|Jdk)$'
        //
        // Note that a comparison is only meaningful against numbers measured the same way, so
        // re-measure both sides of any before/after with the same filter.
        register("spot") {
            include(
                (findProperty("benchmark.spot") as String?)
                    ?: ".*\\.(toString|parse)(Kotlin|Jdk)$"
            )
        }
    }
    targets {
        register("main") {
            this as JvmBenchmarkTarget
            jmhVersion = "1.37"
        }
    }
}
