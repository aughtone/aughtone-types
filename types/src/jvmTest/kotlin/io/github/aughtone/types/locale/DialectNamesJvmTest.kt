package io.github.aughtone.types.locale

import com.ibm.icu.text.LocaleDisplayNames
import com.ibm.icu.util.ULocale
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The bundled dialect names agree with ICU4J's dialect handling, which reads the same CLDR major
 * release. ICU4J is a JVM test dependency only.
 */
class DialectNamesJvmTest {

    @Test
    fun `every bundled dialect name is what ICU4J gives for the dialect form`() {
        val tags = localeResourceMap.values.map { it.languageTag }.toSortedSet()
        val languages = localeResourceMap.values.map { it.languageCode }.toSortedSet()
        val mismatches = mutableListOf<String>()
        var checked = 0
        for (language in languages) {
            val icu = LocaleDisplayNames.getInstance(ULocale(language), LocaleDisplayNames.DialectHandling.DIALECT_NAMES)
            for (tag in tags) {
                val bundled = LocaleDialectNames.nameFor(language, tag) ?: continue
                checked++
                val expected = icu.localeDisplayName(ULocale.forLanguageTag(tag))
                if (bundled != expected) mismatches += "$language $tag: bundled=$bundled icu=$expected"
            }
        }
        println("Dialect names checked against ICU4J: $checked")
        assertEquals(LocaleDialectNames.size, checked)
        assertEquals(emptyList(), mismatches)
    }
}
