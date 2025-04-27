[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
![Maven Central Version](https://img.shields.io/maven-central/v/io.github.aughtone/types?style=flat)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.10-blue.svg?logo=kotlin&style=flat)](http://kotlinlang.org)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-brightgreen?logo=kotlin)](https://github.com/JetBrains/compose-multiplatform)


![badge-android](http://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat)
![badge-ios](http://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat)
![badge-desktop](http://img.shields.io/badge/platform-desktop-DB413D.svg?style=flat)
![badge-js](http://img.shields.io/badge/platform-js%2Fwasm-FDD835.svg?style=flat)

# Multiplatform Types Library

This library provides a collection of common data types for use in [Kotlin Multiplatform](https://www.jetbrains.com/kotlin-multiplatform/) (KMP) projects.

## Overview

The goal of this library is to reduce the duplication of data types across multiple Kotlin Multiplatform libraries and projects. By centralizing these types, we can improve consistency and reduce redundant code.

This library also includes platform-specific types, such as `Locale` and `Currency`, that can be accessed through a common interface.

## Features

* **Financial types:** Currency and Money types.
* **Geo types:** Location at the moment.
* **Quantitative types:** Types that have a specific range of values.
* **URI types:** Various types of URIs.
* **Utility types**: Types taht don't really fint into other categories

## Rational
While developing projects in KMP my peers and I found that all the libraries I wanted to use were duplicating data 
types. 

I decided that at lest for my own work, and for anyone else that was not happy 
with that situation, there could be a library of just types we can all share.

There are also situations where we need to go down to the platform layer to properly get a type, 
and the [Locale] and [Currency] types are examples of that.

There are other things I envision for this library, such as [Duration] style shortcuts for types like [Distance].

Feel free to fork it and make improvements, I'll keep up as best I can.

# Installation
![Maven Central Version](https://img.shields.io/maven-central/v/io.github.aughtone/types?style=flat)

This library is published to Maven Central. You can include it in your project by adding the following to your `build.gradle.kts` (Kotlin) or `build.gradle` (Groovy) file:

#### Groovy (build.gradle)
```gradle
implementation("io.github.aughtone:types:${version}")
```
or 

#### Kotlin Library (libs.cersions.toml)
```gradle
[versions]
aughtone-types = "${version}"

[libraries]
aughtone-types = { module = "io.github.aughtone:types", version.ref = "aughtone-types" }

```
with 

#### Kotlin Gradle (build.gradle.kts)
```gradle
implementation(libs.aughtone.types)
```

# Features

#### Location
The `Location` type consists of a number of quantitative types such as:

```kotlin
val coordinates = Coordinates(latitude = 20.05, longitude = -15.5)
```
```kotlin
val altitude = Altitude(meters = 150.5)
```
```kotlin
val distance = Distance(meters = 150.5)
```

#### Financial

An instance if Currency is included in this library, that attempts to determine the currency based 
on the underlying system (Android, iOS), or by an included resource if the platform doesn't have 
easy access to Currency information (JS, WasmJs, Linux). Where a platform is missing information it 
will be supplemented from the internal resource.

* Example usage:
* ```kotlin
* val caDollar:Currency = currencyFor("CAD")
* val euro:Currency = currencyFor("EUR")
* 
* println("Canadian Dollar code: ${caDollar.code}") // Output: CAD
* println("Euro symbol: ${caDollar.symbol}") // Output: €
* println("Euro number: ${euro.number}") // Output: 978
* println("Euro symbol: ${euro.symbol}") // Output: €
* ```

Currency is also used as part of a Money type, that represents a monetary value with an optional currency. 
```kotlin
val amountInEuro = Money(value = 0.0, currency = currencyFor("EUR"))
```

#### URI & URL

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
