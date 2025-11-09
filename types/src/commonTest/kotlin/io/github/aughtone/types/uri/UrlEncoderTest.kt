package io.github.aughtone.types.uri

import kotlin.test.Test
import kotlin.test.assertEquals

class UrlEncoderTest {
    val testUnencodedParam = "value with space"
    val testEncodedParam = "value+with+space"
    val testMoreThanSpaces = "some/path with:space/and \"more\""
    val testEncodedMoreThanSpaces = "some%2Fpath+with%3Aspace%2Fand+%22more%22"
    val testChineseParam = "你好 你好"
    val testEncodedChineseParam = "你好+你好"

    private val encoder = UrlEncoder

    @Test
    fun testParamWithSpaceIsEncoded() {
        assertEquals(
            testEncodedParam,
            encoder.encode(testUnencodedParam)
        )
    }

    @Test
    fun testParamWithSeveralReservedCharsIsEncoded() {
        assertEquals(
            testEncodedMoreThanSpaces,
            encoder.encode(testMoreThanSpaces)
        )
    }

    @Test
    fun testParamWithChineseIsEncoded() {
        assertEquals(
            testEncodedChineseParam,
            encoder.encode(testChineseParam)
        )
    }
}
