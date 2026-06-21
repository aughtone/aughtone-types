package io.github.aughtone.types.locale

/**
 * Constructs an IETF BCP 47 language tag from the [Locale] instance.
 *
 * This function is maintained for backward compatibility. New code should use the
 * [Locale.languageTag] property.
 *
 * @return A BCP 47 language tag string (e.g., "en-US", "zh-Hans-CN").
 * @see Locale.languageTag
 */
@Deprecated(
    message = "Use the languageTag property instead.",
    replaceWith = ReplaceWith("languageTag")
)
fun Locale.toLanguageTag(): String = languageTag
