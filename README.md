[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
![Maven Central Version](https://img.shields.io/maven-central/v/io.github.aughtone/types?style=flat)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.10-blue.svg?logo=kotlin&style=flat)](http://kotlinlang.org)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-brightgreen?logo=kotlin)](https://github.com/JetBrains/compose-multiplatform)


![badge-android](http://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat)
![badge-ios](http://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat)
![badge-desktop](http://img.shields.io/badge/platform-desktop-DB413D.svg?style=flat)
![badge-js](http://img.shields.io/badge/platform-js%2Fwasm-FDD835.svg?style=flat)

# Multiplatform Types Library

AOTypes is a Kotlin Multiplatform library that provides a collection of strongly-typed data types for common quantitative and financial domains. By using these types, you can improve the clarity, safety, and correctness of your code when dealing with values like money, distance, speed, and more.

**WARNING:** versions of this library in the _1.0.2+_ range are using the `kotlinx-datetime:0.7.1-0.6.x-compat` dependency,
because the switch over to using `kotlin.time.Instant` has not gone very smoothly.

When this library switches over to using `kotlin.time.Instant` in `kotlinx-datetime:0.7.*`, the major
version of this library will change, signaling a breaking change as per standard [semantic versioning](https://semver.org/) practices.

## Rationale
While developing projects in KMP my peers and I found that many libraries were duplicating common data types. I decided that for my own work, and for anyone else who was not happy with that situation, there could be a library of just types we can all share.

This library also abstracts away platform-specific implementations for types like `Locale` and `Currency`, providing them through a common interface.

# Installation
![Maven Central Version](https://img.shields.io/maven-central/v/io.github.aughtone/types?style=flat)

This library is published to Maven Central. You can include it in your project by adding the following to your `build.gradle.kts` (Kotlin) or `build.gradle` (Groovy) file:

#### Groovy (build.gradle)
```gradle
implementation("io.github.aughtone:types:${version}")
```
or

#### Kotlin Library (libs.versions.toml)
```toml
[versions]
aughtone-types = "${version}"

[libraries]
aughtone-types = { module = "io.github.aughtone:types", version.ref = "aughtone-types" }
```
with

#### Kotlin Gradle (build.gradle.kts)
```kotlin
implementation(libs.aughtone.types)
```

# Core Components & Usage

### Financial
This library provides robust types for handling financial data, avoiding common pitfalls of using floating-point numbers for money.

- **`Currency`**: A comprehensive data class representing world currencies, populated from platform-specific data where possible and supplemented with an internal resource.
- **`BankersValue`**: A fixed-point decimal type using Banker's Rounding, ensuring precision in financial calculations.
- **`Money`**: A type that holds a monetary value in cents as a `Long` and uses `BankersValue` for precise calculations.

**Example Usage:**
```kotlin
// Create Money objects
val price = Money(1999L, currencyFor("USD")) // $19.99 from Long (cents)
val shipping = 5.99.toMoney(currencyFor("USD"))  // $5.99 from Double

// Add two Money objects (requires the same currency)
val subtotal = price + shipping
println("Subtotal: ${subtotal.toDouble()}") // Output: Subtotal: 25.98

// Use operators with scalar values
val withTax = subtotal * 1.07 // Apply 7% tax
println("With Tax: ${withTax.toDouble()}") // Output: With Tax: 27.80

// You can also work directly with cents
val discount = withTax - 200L // Subtract 200 cents ($2.00)
println("Final Price: ${discount.toDouble()}") // Output: Final Price: 25.80

// Get the currency for the default locale
val localCurrency = Currency.forLocale()
println("Local currency is: ${localCurrency?.code}")

// Get the currency for a specific locale
val swissCurrency = Currency.forLocale(localeFor("fr-CH"))
println("Currency for Switzerland (French) is: ${swissCurrency?.code}") // Output: Currency for Switzerland (French) is: CHF
```

### Locale
Provides a platform-independent way to work with IETF BCP 47 locales. The library includes a comprehensive resource map of locales and provides functions to access the current native locale of the platform.

- **`Locale`**: A data class representing a locale, including language, region, script, and variant codes.

**Example Usage:**
```kotlin
// Get the current locale for the platform
val currentLocale = Locale.getCurrent()
println("Current Locale: ${currentLocale.displayName}")

// Look up a specific locale by its language tag
val canadianFrench = localeFor("fr-CA")
println(canadianFrench?.displayName) // Output: French (Canada)

// Look up a language-only locale
val german = localeFor("de")
println(german?.displayName) // Output: German
```

### Quantitative & Geospatial
These types provide a safe and expressive way to work with physical measurements and geographical locations.

- **`Coordinates`**: Represents a point on Earth (latitude and longitude).
- **`Location`**: A richer representation that combines `Coordinates` with optional `Altitude` and `Distance` (for accuracy).
- **`Distance`**, **`Speed`**, **`Altitude`**, **`Azimuth`**: Types for handling measurements with their own operators.

**Example Usage:**
```kotlin
// Define coordinates for two cities
val sanFrancisco = Coordinates(37.7749, -122.4194)
val losAngeles = Coordinates(34.0522, -118.2437)

// The '-' operator on Coordinates calculates the distance using the Haversine formula
val distanceBetweenCities = sanFrancisco - losAngeles
println("Distance: %.2f km".format(distanceBetweenCities.meters / 1000))

// The 'plus' operator calculates a new coordinate by moving a set distance and bearing
val startPoint = Coordinates(0.0, 0.0)
val distanceToMove = Distance(111_195.0) // Roughly 1 degree longitude at the equator
val bearing = Azimuth(90.0) // 90 degrees = East
val newPoint = startPoint.plus(distanceToMove, bearing)

println("New coordinates: lat=%.4f, lon=%.4f".format(newPoint.latitude, newPoint.longitude))
```

### Units of Measure
Provides a standardized way to work with different units of measure, preventing errors from mismatched units.

- **`UnitOfMeasure`**: A comprehensive enum of measurement units, from meters and kilograms to bytes and fluid ounces.
- **`MetricPrefix`**: An enum for SI prefixes (kilo, mega, giga, etc.).

**Example Usage:**
```kotlin
// Find a unit by its primary symbol
val kgUnit = UnitOfMeasure.findFirst("kg")
println(kgUnit) // Output: KILOGRAM

// Find all units that share an alternative symbol
val footAndArcMinute = UnitOfMeasure.findAll("'")
println(footAndArcMinute) // Output: [ARC_MINUTE, FOOT]
```

### URI & URL
There are several types that represent common URIs and URLs, including:

* Uniform Resource Name (URN)
* Geo Reference Identifier (GRI)
* Uniform Resource Identifier (URI)
* Uniform Resource Locator (URL)
* Resource Identifier (RI)

# Contributing & Feedback
Contributions to this library are welcome!

Bugs or new features can go into the issue tracker, but you are probably going to get faster support by creating a PR.

## License
This library is licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
