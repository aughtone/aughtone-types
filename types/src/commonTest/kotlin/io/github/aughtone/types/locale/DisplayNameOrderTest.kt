package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * A locale's name comes from this library's own names first, then the platform's, then English,
 * and only then the codes; and a tag the library does not ship keeps its codes and gets an English
 * name from bundled CLDR names, so `kok-IN` is never just "kok-IN" (#48).
 */
class DisplayNameOrderTest {

    private val english = localeFor("en")!!

    @Test
    fun `an unshipped language gets its English name from the bundled CLDR names`() {
        val konkani = parseLocale("kok-IN")
        assertEquals("kok-IN", konkani.languageTag)
        assertEquals("Konkani (India)", konkani.fallbackName)
        assertEquals("Konkani (India)", konkani.localizedDisplayName(displayIn = english))
    }

    @Test
    fun `every subtag is named from bundled data when it can be`() {
        assertEquals("Konkani (Devanagari, Fiji)" to true, composedEnglishName("kok", "Deva", "FJ", null))
        assertEquals("Chinese (Traditional, Singapore)", parseLocale("zh-Hant-SG").fallbackName)
    }

    @Test
    fun `a subtag nothing can name is left as its code`() {
        assertEquals("qqq (Fiji)" to false, composedEnglishName("qqq", null, "FJ", null))
        assertTrue(parseLocale("en-XX").fallbackName.startsWith("English ("))
    }

    @Test
    fun `an English reader gets the shipped table's name on every target`() {
        assertEquals("English (United States)", localeFor("en-US")!!.localizedDisplayName(displayIn = english))
        assertEquals("Chinese (Traditional, Taiwan)", localeFor("zh-TW")!!.localizedDisplayNameOrNull(displayIn = english))
    }

    @Test
    fun `a bundled translation wins over the platform's`() {
        val afrikaans = localeFor("af")!!
        val atikamekw = localeFor("atj-CA")!!
        val bundled = assertNotNull(LocaleDisplayNameSupplement.nameFor("af", "atj-CA"))
        assertEquals(bundled, atikamekw.localizedDisplayNameOrNull(displayIn = afrikaans))
    }

    @Test
    fun `display name is the name in the current language`() {
        val reader = Locale.currentOrNull ?: return
        val enUs = localeFor("en-US")!!
        assertEquals(enUs.localizedDisplayName(displayIn = reader), enUs.displayName)
    }

    @Test
    fun `the device's shipped locale is returned as shipped`() {
        assertEquals(localeFor("fr-CA"), currentLocaleFrom("fr_CA", fallbackTag = "en"))
        assertEquals("French (Canada)", currentLocaleFrom("fr-CA", fallbackTag = null)?.fallbackName)
    }

    @Test
    fun `an unshipped device locale keeps its codes`() {
        val current = currentLocaleFrom("kok-IN", fallbackTag = "en")
        assertEquals("kok-IN", current?.languageTag)
        assertEquals("Konkani (India)", current?.fallbackName)
    }

    @Test
    fun `a remembered current locale is replaced when the device tag changes`() {
        assertEquals(localeFor("fr-CA"), currentLocaleFrom("fr-CA", fallbackTag = null))
        assertEquals(localeFor("de-DE"), currentLocaleFrom("de-DE", fallbackTag = null))
        assertEquals(localeFor("en"), currentLocaleFrom(null, fallbackTag = "en"))
        assertEquals(localeFor("fr"), currentLocaleFrom(null, fallbackTag = "fr"))
        assertEquals(localeFor("fr-CA"), currentLocaleFrom("fr-CA", fallbackTag = null))
    }

    @Test
    fun `the fallback is used only when the device reports nothing usable`() {
        assertEquals(localeFor("en"), currentLocaleFrom(null, fallbackTag = "en"))
        assertEquals(localeFor("en"), currentLocaleFrom("", fallbackTag = "en"))
        assertEquals(localeFor("en"), currentLocaleFrom("1", fallbackTag = "en"))
        assertNull(currentLocaleFrom(null, fallbackTag = null))
    }

    @Test
    fun `the bundled CLDR name tables hold what was generated`() {
        assertEquals(637, EnglishSubtagNames.languages.size)
        assertEquals(213, EnglishSubtagNames.scripts.size)
        assertEquals(295, EnglishSubtagNames.regions.size)
        assertEquals("Konkani", EnglishSubtagNames.languages["kok"])
        assertEquals("Traditional", EnglishSubtagNames.scripts["Hant"])
        assertEquals("Latin America", EnglishSubtagNames.regions["419"])
    }

    @Test
    fun `every shipped name agrees with the subtag names read from the table`() {
        // Subtag names are read first-wins from the shipped names, so two entries naming one region
        // differently would silently pick one. Hold every entry to what was read.
        val names = shippedSubtagNames
        val disagreements = mutableListOf<String>()
        for (locale in localeResourceMap.values) {
            val name = locale.fallbackName
            if (name.substringBefore(" (") != names.languages[locale.languageCode]) {
                disagreements += "${locale.languageTag}: language in '$name'"
            }
            if (locale.variantCode != null || " (" !in name) continue
            val qualifiers = name.substringAfter(" (").dropLast(1).split(", ")
            val region = locale.regionCode
            if (region != null && qualifiers.last() != names.regions[region]) {
                disagreements += "${locale.languageTag}: region in '$name' vs '${names.regions[region]}'"
            }
            val script = locale.scriptCode
            val scriptPart = if (region != null) qualifiers.takeIf { it.size == 2 }?.first() else qualifiers.singleOrNull()
            if (script != null && scriptPart != null && scriptPart != names.scripts[script]) {
                disagreements += "${locale.languageTag}: script in '$name' vs '${names.scripts[script]}'"
            }
        }
        if (disagreements.isNotEmpty()) fail(disagreements.joinToString("\n"))
    }
}
