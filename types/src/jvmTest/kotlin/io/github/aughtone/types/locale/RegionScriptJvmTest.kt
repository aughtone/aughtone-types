package io.github.aughtone.types.locale

import com.ibm.icu.util.ULocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Checks the region-script table and [resolveLocale] against CLDR's likely subtags, through ICU, for
 * every shipped language and ISO 3166 country. (#50)
 */
class RegionScriptJvmTest {

    private val languages = localeResourceMap.values.map { it.languageCode }.toSortedSet()
    private val countries = ULocale.getISOCountries().sorted()

    private fun likelyScript(tag: String): String = ULocale.addLikelySubtags(ULocale.forLanguageTag(tag)).script

    @Test
    fun `the table holds every pair whose script differs from the language's default`() {
        val expected = buildMap {
            for (language in languages) {
                val default = likelyScript(language)
                for (country in countries) {
                    val script = likelyScript("$language-$country")
                    if (script.isNotEmpty() && script != default) put("$language-$country", script)
                }
            }
        }
        assertEquals(expected, scriptByLanguageRegion)
    }

    @Test
    fun `every language-region tag resolves in CLDR's likely script`() {
        val wrong = mutableListOf<String>()
        for (language in languages) {
            // Plain sr ships in Latin, against CLDR's Cyrillic default, which #51 is to decide.
            if (language == "sr") continue
            for (country in countries) {
                val tag = "$language-$country"
                val likely = likelyScript(tag).ifEmpty { continue }
                val resolved = resolveLocale(tag) ?: continue
                if (resolved.languageCode == language && resolved.effectiveScriptCode != likely) {
                    wrong += "$tag -> ${resolved.languageTag} (${resolved.effectiveScriptCode}, CLDR $likely)"
                }
            }
        }
        assertTrue(wrong.isEmpty(), "Resolved in another script:\n" + wrong.joinToString("\n"))
    }
}
