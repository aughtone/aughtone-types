package io.github.aughtone.types.financial

actual fun currencyForNative(currencyCode: String): Currency? {
    // Browsers do not have a native API to look up arbitrary currency data.
    // We fall back to the shared resource map.
    return currencyFor(currencyCode)
}
