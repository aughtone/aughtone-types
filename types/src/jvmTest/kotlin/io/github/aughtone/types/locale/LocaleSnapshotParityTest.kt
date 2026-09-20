package io.github.aughtone.types.locale

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Guards `docs/reference/supported_languages.json` against the locale table that actually ships.
 *
 * The snapshot is a second copy of `localeResourceMap` kept for provenance, and nothing in the build
 * binds the two together — which is exactly how they drifted 34 entries apart once already, silently,
 * when languages were added to the Kotlin table and not back to the snapshot.
 *
 * This is a JVM-only test because the snapshot lives under `docs/` rather than in a source set, so it
 * is not on any target's classpath and can only be reached through file I/O.
 *
 * If this fails, **the Kotlin table is the source of truth**: regenerate the snapshot from it. Do not
 * edit the snapshot by hand to match, and do not regenerate the Kotlin table from CLDR — the table is
 * first-party curated data whose differences from CLDR are deliberate. See `docs/reference/README.md`.
 */
class LocaleSnapshotParityTest {

    @Serializable
    private data class SnapshotEntry(
        val languageTag: String,
        val languageCode: String,
        val regionCode: String? = null,
        val scriptCode: String? = null,
        val variantCode: String? = null,
        val displayName: String,
    )

    @Serializable
    private data class Snapshot(val locales: List<SnapshotEntry>)

    private val snapshotFile = File("../docs/reference/supported_languages.json")

    private fun snapshot(): Map<String, Locale> {
        assertTrue(
            snapshotFile.isFile,
            "Locale snapshot not found at ${snapshotFile.canonicalPath}. " +
                "This test resolves it relative to the module directory.",
        )
        val parsed = Json { ignoreUnknownKeys = false }
            .decodeFromString(Snapshot.serializer(), snapshotFile.readText())
        val duplicates = parsed.locales.groupingBy { it.languageTag }.eachCount().filterValues { it > 1 }
        assertTrue(duplicates.isEmpty(), "Snapshot has duplicate language tags: ${duplicates.keys.sorted()}")
        return parsed.locales.associate {
            it.languageTag to Locale(
                languageCode = it.languageCode,
                regionCode = it.regionCode,
                scriptCode = it.scriptCode,
                variantCode = it.variantCode,
                displayName = it.displayName,
            )
        }
    }

    @Test
    fun `the locale snapshot lists exactly the locales the library ships`() {
        val snapshot = snapshot()
        val missing = (localeResourceMap.keys - snapshot.keys).sorted()
        val extra = (snapshot.keys - localeResourceMap.keys).sorted()
        if (missing.isNotEmpty() || extra.isNotEmpty()) {
            fail(
                buildString {
                    append("Locale snapshot is out of sync with localeResourceMap.")
                    if (missing.isNotEmpty()) {
                        append("\n  Shipped but absent from the snapshot: ${missing.joinToString(" ")}")
                    }
                    if (extra.isNotEmpty()) {
                        append("\n  In the snapshot but not shipped: ${extra.joinToString(" ")}")
                    }
                    append("\n  Regenerate the snapshot from the Kotlin table; see docs/reference/README.md.")
                },
            )
        }
        assertEquals(localeResourceMap.size, snapshot.size)
    }

    @Test
    fun `every snapshot entry matches the shipped locale field for field`() {
        val snapshot = snapshot()
        val differing = localeResourceMap.keys
            .filter { it in snapshot }
            .filter { localeResourceMap.getValue(it) != snapshot.getValue(it) }
            .sorted()
        if (differing.isNotEmpty()) {
            fail(
                buildString {
                    append("Locale snapshot disagrees with localeResourceMap for ${differing.size} tag(s):")
                    differing.take(20).forEach { tag ->
                        append("\n  $tag\n    shipped : ${localeResourceMap.getValue(tag)}")
                        append("\n    snapshot: ${snapshot.getValue(tag)}")
                    }
                    if (differing.size > 20) append("\n  ... and ${differing.size - 20} more.")
                    append("\n  Regenerate the snapshot from the Kotlin table; see docs/reference/README.md.")
                },
            )
        }
    }
}
