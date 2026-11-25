package io.github.aughtone.types.locale

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

/**
 * [Locale] is identified by its language tag, normalizes the case of its codes however it was made,
 * and keeps the JSON shape it had as a data class.
 */
class LocaleIdentityTest {

    private val enUs = localeFor("en-US")!!

    @Test
    fun `codes are case normalized when built`() {
        val locale = Locale("EN", "us", "LATN", "POSIX", displayName = "x")
        assertEquals("en", locale.languageCode)
        assertEquals("US", locale.regionCode)
        assertEquals("Latn", locale.scriptCode)
        assertEquals("posix", locale.variantCode)
        assertEquals("en-Latn-US-posix", locale.languageTag)
    }

    @Test
    fun `blank optional codes count as absent`() {
        val locale = Locale("en", regionCode = "", scriptCode = " ", variantCode = "", displayName = "x")
        assertNull(locale.regionCode)
        assertNull(locale.scriptCode)
        assertNull(locale.variantCode)
        assertEquals("en", locale.languageTag)
    }

    @Test
    fun `the display name takes no part in equality`() {
        val renamed = Locale("en", "US", displayName = "Anglais (États-Unis)")
        assertEquals(enUs, renamed)
        assertEquals(enUs.hashCode(), renamed.hashCode())
        assertEquals(1, setOf(enUs, renamed).size)
        assertEquals("first", mapOf(enUs to "first")[renamed])
    }

    @Test
    fun `a different tag is a different locale`() {
        assertNotEquals(enUs, localeFor("en-GB"))
        assertNotEquals(enUs, Locale("en", "US", variantCode = "posix", displayName = enUs.fallbackName))
    }

    @Test
    fun `a locale prints as its tag`() {
        assertEquals("zh-Hant-TW", localeFor("zh-TW").toString())
    }

    @Test
    fun `the JSON shape is unchanged`() {
        val zhTw = localeFor("zh-TW")!!
        assertEquals(
            """{"languageCode":"zh","regionCode":"TW","scriptCode":"Hant","variantCode":null,""" +
                """"displayName":"Chinese (Traditional, Taiwan)","languageTag":"zh-Hant-TW"}""",
            Json { encodeDefaults = true }.encodeToString(Locale.serializer(), zhTw),
        )
        assertEquals(
            """{"languageCode":"en","displayName":"English"}""",
            Json.encodeToString(Locale.serializer(), localeFor("en")!!),
        )
    }

    @Test
    fun `a decoded payload is case normalized and its stored tag ignored`() {
        val decoded = Json.decodeFromString(
            Locale.serializer(),
            """{"languageCode":"EN","regionCode":"us","displayName":"English","languageTag":"fr"}""",
        )
        assertEquals("en-US", decoded.languageTag)
        assertEquals(enUs, decoded)
    }

    @Test
    fun `a payload written by an earlier release still decodes`() {
        val json = Json { encodeDefaults = true }
        val encoded = json.encodeToString(Locale.serializer(), enUs)
        val decoded = json.decodeFromString(Locale.serializer(), encoded)
        assertEquals(enUs, decoded)
        assertEquals(enUs.fallbackName, decoded.fallbackName)
    }
}
