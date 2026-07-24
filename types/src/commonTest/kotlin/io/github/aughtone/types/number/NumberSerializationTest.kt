package io.github.aughtone.types.number

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class NumberSerializationTest {

    @Test
    fun `big integer json round trip`() {
        val values = listOf(
            "0", "1", "-1", "12345678901234567890",
            "-999999999999999999999999999999", "4294967296"
        )
        for (s in values) {
            val original = BigInteger(s)
            val json = Json.encodeToString(BigInteger.serializer(), original)
            val decoded = Json.decodeFromString(BigInteger.serializer(), json)
            assertEquals(original, decoded, "round trip for $s")
            assertEquals(s, decoded.toString())
        }
    }

    @Test
    fun `big decimal json round trip`() {
        val values = listOf("0", "0.00", "1.23", "-123.456", "6E+2", "0.000123")
        for (s in values) {
            val original = BigDecimal(s)
            val json = Json.encodeToString(BigDecimal.serializer(), original)
            val decoded = Json.decodeFromString(BigDecimal.serializer(), json)
            assertEquals(original, decoded, "round trip for $s")
            assertEquals(s, decoded.toString())
        }
    }

    @Test
    fun `bankers value json round trip`() {
        for (cents in listOf(0L, 1L, -1L, 12345L, Long.MAX_VALUE)) {
            val original = BankersValue.fromLong(cents)
            val json = Json.encodeToString(BankersValue.serializer(), original)
            assertEquals(original, Json.decodeFromString(BankersValue.serializer(), json))
        }
    }

    @Test
    fun `invalid big integer payloads are rejected`() {
        // signum != 0 with empty magnitude
        assertFails {
            Json.decodeFromString(BigInteger.serializer(), """{"signum":1,"magnitude":[]}""")
        }
        // unstripped leading zero word
        assertFails {
            Json.decodeFromString(BigInteger.serializer(), """{"signum":1,"magnitude":[1,0]}""")
        }
        // signum out of range
        assertFails {
            Json.decodeFromString(BigInteger.serializer(), """{"signum":2,"magnitude":[1]}""")
        }
        // zero signum with non-empty magnitude
        assertFails {
            Json.decodeFromString(BigInteger.serializer(), """{"signum":0,"magnitude":[1]}""")
        }
    }
}
