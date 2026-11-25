package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

/**
 * Locales that differ only by script must not share a localized name. Android built the platform
 * locale without its script, so `zh-Hans` and `zh-Hant` were both "Chinese". (#52)
 */
class ScriptInLocalizedNameTest {

    private val english = assertNotNull(localeFor("en"))
    private val french = assertNotNull(localeFor("fr"))

    @Test
    fun `Simplified and Traditional Chinese have different names`() {
        val simplified = assertNotNull(localeFor("zh-Hans"))
        val traditional = assertNotNull(localeFor("zh-Hant"))
        for (displayIn in listOf(english, french)) {
            assertNotEquals(
                simplified.localizedDisplayName(displayIn),
                traditional.localizedDisplayName(displayIn),
                "zh-Hans and zh-Hant named alike in ${displayIn.languageTag}",
            )
        }
    }

    @Test
    fun `Latin and syllabic Inuktitut have different names`() {
        val syllabic = assertNotNull(localeFor("iu-CA"))
        val latin = parseLocale("iu-Latn-CA")
        for (displayIn in listOf(english, french)) {
            assertNotEquals(
                syllabic.localizedDisplayName(displayIn),
                latin.localizedDisplayName(displayIn),
                "iu-CA and iu-Latn-CA named alike in ${displayIn.languageTag}",
            )
        }
    }
}
