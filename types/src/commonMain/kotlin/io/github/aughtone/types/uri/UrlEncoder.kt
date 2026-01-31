package io.github.aughtone.types.uri


/**
 * Utility object for URL encoding strings.
 *
 * This class provides a method to encode strings according to the URL encoding rules,
 * which involves replacing unsafe characters with their percent-encoded equivalents (e.g., "%20" for a space).
 * It supports UTF-8 encoding for characters outside the basic ASCII range.
 *
 * It's based in RFC 3986 rules.
 */
object UrlEncoder {
    /**
     * Set of characters that are considered safe (unreserved) and do not need to be encoded.
     */
    private val UNRESERVED_CHARS = setOf(
        '-', '_', '.', '*' // RFC 3986 unreserved characters
    )

    /**
     * Encodes a string according to URL encoding rules.
     *
     * This function takes a string and encodes it to a URL-safe format.
     * It follows the following rules:
     * - Alphanumeric characters and characters in [UNRESERVED_CHARS] are left as-is.
     * - Spaces are encoded as '+'.
     * - All other characters are encoded using percent-encoding (%XX),
     *   where XX is the uppercase hexadecimal representation of the character's UTF-8 byte value.
     *
     * @param value The string to be encoded.
     * @return The URL-encoded string.
     */
    fun encode(value: String): String {
        val encoded = StringBuilder()

        for (char in value) {
            when {
                char.isLetterOrDigit() || UNRESERVED_CHARS.contains(char) -> {
                    encoded.append(char) // Safe characters can be directly appended
                }

                char == ' ' -> {
                    encoded.append("+") // Spaces are often encoded as '+'
                }

                else -> {
                    // Encode other characters using %XX (percent-encoding)
                    val bytes = encodeToUtf8(char)
                    for (byte in bytes) {
                        encoded.append("%")
                        encoded.append(byteToHex(byte)) // Format as two uppercase hex digits
                    }
                }
            }
        }

        return encoded.toString()
    }

    /**
     * Encodes a single character to its UTF-8 byte representation.
     *
     * @param char The character to encode.
     * @return The UTF-8 encoded byte array.
     */
    private fun encodeToUtf8(char: Char): ByteArray {
        return when {
            char.code <= 0x7F -> byteArrayOf(char.code.toByte())
            char.code <= 0x7FF -> {
                val code = char.code
                val byte1 = (0b11000000 or ((code shr 6) and 0b00011111)).toByte()
                val byte2 = (0b10000000 or (code and 0b00111111)).toByte()
                byteArrayOf(byte1, byte2)
            }

            char.code <= 0xFFFF -> {
                val code = char.code
                val byte1 = (0b11100000 or ((code shr 12) and 0b00001111)).toByte()
                val byte2 = (0b10000000 or ((code shr 6) and 0b00111111)).toByte()
                val byte3 = (0b10000000 or (code and 0b00111111)).toByte()
                byteArrayOf(byte1, byte2, byte3)
            }

            else -> throw IllegalArgumentException("Unsupported character: ${char.code}")
        }
    }

    /**
     * Converts a byte to its two-digit uppercase hexadecimal string representation.
     *
     * @param byte The byte to convert.
     * @return The two-digit uppercase hexadecimal string.
     */
    private fun byteToHex(byte: Byte): String {
        val unsignedByte = byte.toInt() and 0xFF // Convert to unsigned (0-255)
        val hexChars = "0123456789ABCDEF"
        val highNibble = (unsignedByte shr 4) and 0x0F // Get the upper 4 bits
        val lowNibble = unsignedByte and 0x0F // Get the lower 4 bits
        return "${hexChars[highNibble]}${hexChars[lowNibble]}" // Lookup characters and combine
    }

}
