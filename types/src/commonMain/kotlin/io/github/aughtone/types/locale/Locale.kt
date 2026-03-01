package io.github.aughtone.types.locale

data class Locale(
    val languageCode: String,
    val regionCode: String? = null,
    val scriptCode: String? = null,
    val variantCode: String? = null,
    val displayName: String
) {
    companion object
}
