package io.github.aughtone.types.locale

import io.github.aughtone.types.financial.Currency
import io.github.aughtone.types.financial.currencyFor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Represents a specific geographical, political, or cultural region, conforming to
 * IETF BCP 47 standards.
 *
 * This class provides a platform-independent way to handle locale information, integrating:
 * - **ISO 639**: Language codes (e.g., "en", "fr").
 * - **ISO 3166**: Country/Region codes (e.g., "US", "FR", "001").
 * - **ISO 15924**: Script codes (e.g., "Latn", "Hans").
 *
 * It is used to localize data, such as formatting numbers, currencies, and dates, or
 * for selecting language-specific resources in a multiplatform context.
 *
 * **Preference**: This type is intended for use in `commonMain` code to ensure consistency
 * across targets. While platform-native types like `java.util.Locale` or framework types
 * like Compose's `Locale` exist, this version is preferred for cross-platform interoperability.
 *
 * **Identity is the language tag.** Two locales are equal when their [languageTag] is, whatever
 * their [displayName] says, so a locale built from the shipped table and one decoded from a payload
 * that named it differently are the same value, in a `Set` and as a map key. That is what lets
 * [displayName] follow the reader's language without changing what the locale is.
 *
 * **Case is normalized on the way in**, as BCP 47 asks: the language and variant lowercase, the
 * script in title case and the region uppercase, so `Locale("EN", "us", ...)` is `en-US`. A blank
 * region, script or variant counts as absent. This applies however the value was made, including
 * when it is decoded.
 *
 * This is not a data class, because two of the things a data class adds are wrong for it: `copy`
 * could change a code and keep a name that no longer describes it, and positional destructuring of
 * four codes and a name means nothing. Build a locale from the companion's `invoke`, which reads like
 * a constructor call, or look one up with [localeFor] or [resolveLocale].
 *
 */
@Serializable
class Locale private constructor(
    // The wire carries the codes as they were written; the public properties below normalize them,
    // so a payload with "EN" decodes to "en" without a custom serializer.
    @SerialName("languageCode")
    private val encodedLanguageCode: String,
    @SerialName("regionCode")
    private val encodedRegionCode: String? = null,
    @SerialName("scriptCode")
    private val encodedScriptCode: String? = null,
    @SerialName("variantCode")
    private val encodedVariantCode: String? = null,
    // The name the locale was built with: the shipped table's English name, an English name a
    // lookup composed, or whatever a caller passed. It is the last resort for displayName and the
    // English answer where nothing better is known, and the only name that goes on the wire.
    @SerialName("displayName")
    internal val fallbackName: String,
) {
    /**
     * The name of this locale in the user's current language, such as "Konkani (India)" for `kok-IN`
     * when the user reads English and "कोंकणी (भारत)" when they read Konkani.
     *
     * The language is that of [Locale.currentOrNull], read each time, so the name follows the user if
     * they change it. It comes from the first of these that has one:
     *
     * 1. **This library's own names**: the shipped locale table, which is English, or the bundled
     *    translations into the user's language. Where they have a name, it is the same on every target.
     * 2. **The platform's name** in the user's language. Wording here varies between platforms.
     * 3. **An English name**, when nothing names the locale in the user's language.
     * 4. **The codes**, only for a subtag nothing at all can name.
     *
     * This is [localizedDisplayName] with the user's language filled in; call that to name a locale
     * in a language of your choosing, English included. Because the name depends on the device, never
     * compare it, key on it or snapshot it: compare locales, which are equal by [languageTag].
     */
    val displayName: String
        get() {
            val reader = currentOrNull
            val key = reader?.languageTag
            displayNameCache?.let { (cachedFor, name) -> if (cachedFor == key) return name }
            val name = if (reader == null) englishName() else localizedDisplayName(displayIn = reader)
            // A benign race: two threads may both compute it, and either result is correct.
            displayNameCache = key to name
            return name
        }

    @Transient
    private var displayNameCache: Pair<String?, String>? = null

    /** The ISO 639 language code, lowercase (e.g., "en", "atj"). */
    @Transient
    val languageCode: String = encodedLanguageCode.lowercase()

    /** The ISO 3166-1 alpha-2 country code or UN M.49 numeric-3 area code, uppercase, or `null`. */
    @Transient
    val regionCode: String? = encodedRegionCode?.takeIf { it.isNotBlank() }?.uppercase()

    /** The ISO 15924 script code, in title case (e.g., "Latn", "Hant"), or `null`. */
    @Transient
    val scriptCode: String? = encodedScriptCode?.takeIf { it.isNotBlank() }
        ?.lowercase()?.replaceFirstChar { it.uppercaseChar() }

    /** Any variant subtags, lowercase, or `null`. */
    @Transient
    val variantCode: String? = encodedVariantCode?.takeIf { it.isNotBlank() }?.lowercase()

    /**
     * The IETF BCP 47 language tag representation of this locale, and its identity.
     *
     * The tag is constructed by joining the non-null components with hyphens in the following order:
     * `languageCode`, `scriptCode`, `regionCode`, `variantCode`.
     *
     * Examples:
     * - `en` (language only)
     * - `en-US` (language and region)
     * - `zh-Hans` (language and script)
     * - `zh-Hans-CN` (language, script, and region)
     * - `es-419` (language and UN M.49 numeric region, carried in [regionCode])
     */
    @Transient
    val languageTag: String = buildString {
        append(languageCode)
        if (scriptCode != null) {
            append("-")
            append(scriptCode)
        }
        if (regionCode != null) {
            append("-")
            append(regionCode)
        }
        if (variantCode != null) {
            append("-")
            append(variantCode)
        }
    }

    // Earlier releases serialized the tag, so a payload may still carry it. It is read and ignored,
    // because the tag is derived from the codes, and written exactly as before so the wire is unchanged.
    @SerialName("languageTag")
    private val encodedLanguageTag: String = languageTag

    /** Two locales are equal when their [languageTag] is; [displayName] takes no part. */
    override fun equals(other: Any?): Boolean =
        this === other || (other is Locale && languageTag == other.languageTag)

    /** Hashes the [languageTag] alone, so locales equal under [equals] share a bucket. */
    override fun hashCode(): Int = languageTag.hashCode()

    /** The [languageTag]. */
    override fun toString(): String = languageTag

    companion object {
        /**
         * Creates a [Locale] from its codes and name. Codes are case-normalized, so `"EN"` and `"us"`
         * give `en-US`; see [Locale].
         *
         * @param languageCode The ISO 639-1 alpha-2 or alpha-3 language code.
         * @param regionCode The ISO 3166-1 alpha-2 country code or UN M.49 numeric-3 area code.
         * @param scriptCode The ISO 15924 alpha-4 script code.
         * @param variantCode Any arbitrary value used to indicate a variation of a [Locale].
         * @param displayName An English name for the locale. [Locale.displayName] uses it only when
         *   neither this library nor the platform can name the locale in the reader's language, and
         *   in English only when this library does not ship the locale; see [Locale.displayName].
         */
        operator fun invoke(
            languageCode: String,
            regionCode: String? = null,
            scriptCode: String? = null,
            variantCode: String? = null,
            displayName: String,
        ): Locale = Locale(
            encodedLanguageCode = languageCode,
            encodedRegionCode = regionCode,
            encodedScriptCode = scriptCode,
            encodedVariantCode = variantCode,
            fallbackName = displayName,
        )

        /**
         * Returns the system's current default [Locale].
         *
         * This property provides a platform-independent way to access the user's active
         * locale settings. It is resolved using native platform APIs.
         *
         * @throws IllegalArgumentException if the system locale cannot be determined. Use
         *   [currentOrNull] to receive `null` instead.
         * @see currentOrNull
         * @see currentNativeLocale
         */
        val current: Locale
            get() = requireNotNull(currentNativeLocale()) { "Could not determine your locale. Try using getLocale(languageTag) or construct your own." }

        /**
         * Returns the system's current default [Locale], or `null` when the platform cannot say.
         *
         * The same lookup as [current], reported rather than thrown. Reach for this wherever a
         * sensible fallback exists and an exception would be the wrong shape — a default argument,
         * a property initialiser, a composable — since [current] throws and those are all places a
         * throw is hard to recover from.
         *
         * Prefer [current] when there is no fallback: failing loudly beats silently rendering
         * someone else's language.
         *
         * @return The current [Locale], or `null` if the platform could not resolve one.
         */
        val currentOrNull: Locale?
            get() = currentNativeLocale()


        /**
         * Returns the [Locale] associated with the provided IETF BCP 4 language tag.
         */
        fun getLocale(languageTag: String): Locale? =
            resolveLocale(languageTag)
    }
}
