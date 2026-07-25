package io.github.aughtone.types.financial

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class FinancialSerializationTest {
    // encodeDefaults avoids evaluating the Currency.current default during encoding.
    private val json = Json { encodeDefaults = true }

    private val usd = Currency(
        code = "USD",
        number = 840,
        digits = 2,
        name = "US Dollar",
        symbol = "$"
    )

    @Test
    fun `currency serialization round trip`() {
        val encoded = json.encodeToString(Currency.serializer(), usd)
        assertEquals(usd, json.decodeFromString(Currency.serializer(), encoded))
    }

    @Test
    fun `money serialization round trip`() {
        val money = Money(12345L, usd)
        val encoded = json.encodeToString(Money.serializer(), money)
        assertEquals(money, json.decodeFromString(Money.serializer(), encoded))
    }

    @Test
    fun `negative money serialization round trip`() {
        val money = Money(-1050L, usd)
        val encoded = json.encodeToString(Money.serializer(), money)
        assertEquals(money, json.decodeFromString(Money.serializer(), encoded))
    }
}
