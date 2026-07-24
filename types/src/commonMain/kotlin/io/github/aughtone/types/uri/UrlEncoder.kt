package io.github.aughtone.types.uri


/**
 * Utility object for percent-encoding and decoding strings.
 *
 * [encode]/[decode] follow RFC 3986: only ASCII unreserved characters
 * (ALPHA / DIGIT / "-" / "." / "_" / "~") are left bare, all other characters
 * (including space, as "%20") are percent-encoded as their UTF-8 bytes using
 * uppercase hexadecimal digits.
 *
 * [encodeFormData]/[decodeFormData] follow the legacy
 * `application/x-www-form-urlencoded` rules where space maps to "+",
 * "*" is left bare and "~" is encoded.
 */
object UrlEncoder {

    private fun isUnreserved(char: Char): Boolean =
        char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' ||
                char == '-' || char == '.' || char == '_' || char == '~'

    private fun isFormSafe(char: Char): Boolean =
        char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' ||
                char == '-' || char == '.' || char == '_' || char == '*'

    /**
     * Percent-encodes [value] according to RFC 3986.
     *
     * Characters outside the ASCII unreserved set are encoded as the uppercase
     * hexadecimal representation of their UTF-8 bytes. Surrogate pairs are
     * combined into a single code point before encoding.
     *
     * @param value The string to be encoded.
     * @return The percent-encoded string.
     * @throws IllegalArgumentException if [value] contains an unpaired surrogate.
     */
    fun encode(value: String): String = encodeInternal(value, ::isUnreserved, spaceAsPlus = false)

    /**
     * Encodes [value] as `application/x-www-form-urlencoded` form data:
     * space becomes "+", "*" is left bare and "~" is percent-encoded.
     *
     * @param value The string to be encoded.
     * @return The form-encoded string.
     * @throws IllegalArgumentException if [value] contains an unpaired surrogate.
     */
    fun encodeFormData(value: String): String =
        encodeInternal(value, ::isFormSafe, spaceAsPlus = true)

    private fun encodeInternal(
        value: String,
        isSafe: (Char) -> Boolean,
        spaceAsPlus: Boolean,
    ): String {
        val encoded = StringBuilder()
        var i = 0
        while (i < value.length) {
            val char = value[i]
            when {
                isSafe(char) -> {
                    encoded.append(char)
                    i++
                }

                spaceAsPlus && char == ' ' -> {
                    encoded.append('+')
                    i++
                }

                else -> {
                    // Combine surrogate pairs so supplementary characters encode
                    // as real UTF-8 (4 bytes), not CESU-8.
                    val codePoint: Int
                    if (char.isHighSurrogate() && i + 1 < value.length && value[i + 1].isLowSurrogate()) {
                        codePoint =
                            0x10000 + ((char.code - 0xD800) shl 10) + (value[i + 1].code - 0xDC00)
                        i += 2
                    } else {
                        require(!char.isSurrogate()) { "Unpaired surrogate at index $i" }
                        codePoint = char.code
                        i++
                    }
                    for (byte in encodeUtf8(codePoint)) {
                        encoded.append('%')
                        encoded.append(byteToHex(byte))
                    }
                }
            }
        }
        return encoded.toString()
    }

    /**
     * Decodes a percent-encoded string according to RFC 3986.
     *
     * "+" is kept as a literal plus sign; use [decodeFormData] for form data.
     *
     * @param value The string to decode.
     * @return The decoded string.
     * @throws IllegalArgumentException if [value] contains a malformed percent
     * sequence or the decoded bytes are not valid UTF-8.
     */
    fun decode(value: String): String = decodeInternal(value, plusAsSpace = false)

    /**
     * Decodes `application/x-www-form-urlencoded` form data:
     * "+" becomes a space, then percent sequences are decoded.
     *
     * @param value The string to decode.
     * @return The decoded string.
     * @throws IllegalArgumentException if [value] contains a malformed percent
     * sequence or the decoded bytes are not valid UTF-8.
     */
    fun decodeFormData(value: String): String = decodeInternal(value, plusAsSpace = true)

    private fun decodeInternal(value: String, plusAsSpace: Boolean): String {
        val out = StringBuilder()
        val bytes = mutableListOf<Byte>()

        fun flush() {
            if (bytes.isNotEmpty()) {
                val decoded = try {
                    bytes.toByteArray().decodeToString(throwOnInvalidSequence = true)
                } catch (e: CharacterCodingException) {
                    throw IllegalArgumentException("Invalid UTF-8 in percent-encoded sequence", e)
                }
                out.append(decoded)
                bytes.clear()
            }
        }

        var i = 0
        while (i < value.length) {
            val char = value[i]
            if (char == '%') {
                require(i + 2 < value.length) { "Incomplete percent sequence at index $i" }
                bytes.add(((hexValue(value[i + 1], i) shl 4) or hexValue(value[i + 2], i)).toByte())
                i += 3
            } else {
                flush()
                out.append(if (plusAsSpace && char == '+') ' ' else char)
                i++
            }
        }
        flush()
        return out.toString()
    }

    private fun hexValue(char: Char, at: Int): Int = when (char) {
        in '0'..'9' -> char - '0'
        in 'A'..'F' -> char - 'A' + 10
        in 'a'..'f' -> char - 'a' + 10
        else -> throw IllegalArgumentException("Invalid hex digit '$char' in percent sequence at index $at")
    }

    /**
     * Encodes a single Unicode code point to its UTF-8 byte representation.
     */
    private fun encodeUtf8(codePoint: Int): ByteArray = when {
        codePoint <= 0x7F -> byteArrayOf(codePoint.toByte())
        codePoint <= 0x7FF -> byteArrayOf(
            (0xC0 or (codePoint shr 6)).toByte(),
            (0x80 or (codePoint and 0x3F)).toByte(),
        )

        codePoint <= 0xFFFF -> byteArrayOf(
            (0xE0 or (codePoint shr 12)).toByte(),
            (0x80 or ((codePoint shr 6) and 0x3F)).toByte(),
            (0x80 or (codePoint and 0x3F)).toByte(),
        )

        else -> byteArrayOf(
            (0xF0 or (codePoint shr 18)).toByte(),
            (0x80 or ((codePoint shr 12) and 0x3F)).toByte(),
            (0x80 or ((codePoint shr 6) and 0x3F)).toByte(),
            (0x80 or (codePoint and 0x3F)).toByte(),
        )
    }

    /**
     * Converts a byte to its two-digit uppercase hexadecimal string representation.
     */
    private fun byteToHex(byte: Byte): String {
        val unsignedByte = byte.toInt() and 0xFF
        val hexChars = "0123456789ABCDEF"
        return "${hexChars[unsignedByte shr 4]}${hexChars[unsignedByte and 0x0F]}"
    }

}
