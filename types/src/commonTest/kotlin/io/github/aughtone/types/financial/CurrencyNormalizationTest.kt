package io.github.aughtone.types.financial

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/** [Currency] uppercases its codes however it was made, prints as its code, and keeps its JSON shape. */
class CurrencyNormalizationTest {

    private val usd = currencyFor("USD")!!

    @Test
    fun `codes are uppercased when built`() {
        val built = Currency("usd", 840, 2, "US Dollar", "$")
        assertEquals("USD", built.code)
        assertEquals(usd, built)
        assertEquals("SRD", Currency("srg", 740, 2, "Surinam Guilder", "", obsolete = true, replacedBy = "srd").replacedBy)
    }

    @Test
    fun `a currency prints as its code`() {
        assertEquals("USD", usd.toString())
    }

    @Test
    fun `the JSON shape is unchanged`() {
        assertEquals(
            """{"code":"USD","number":840,"digits":2,"name":"US Dollar","symbol":"$","obsolete":false,"replacedBy":null}""",
            Json { encodeDefaults = true }.encodeToString(Currency.serializer(), usd),
        )
    }

    @Test
    fun `a decoded payload is uppercased`() {
        val decoded = Json.decodeFromString(
            Currency.serializer(),
            """{"code":"usd","number":840,"digits":2,"name":"US Dollar","symbol":"$"}""",
        )
        assertEquals("USD", decoded.code)
        assertEquals(usd, decoded)
    }
}
