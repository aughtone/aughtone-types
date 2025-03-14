[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
![Maven Central Version](https://img.shields.io/maven-central/v/io.github.aughtone/framework-types?style=flat)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.10-blue.svg?logo=kotlin&style=flat)](http://kotlinlang.org)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-brightgreen?logo=kotlin)](https://github.com/JetBrains/compose-multiplatform)


![badge-android](http://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat)
![badge-ios](http://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat)
![badge-desktop](http://img.shields.io/badge/platform-desktop-DB413D.svg?style=flat)
![badge-js](http://img.shields.io/badge/platform-js%2Fwasm-FDD835.svg?style=flat)


# Multiplatform Types

This library for set up for [Kotlin Multiplatform](https://www.jetbrains.com/kotlin-multiplatform/) (KMP)

While developing project for KMP I fond that all the libraries i wanted to ue were duplicating data 
types, like LatLong. I decided that at lest for myown work, and for anyone else that was not happy 
with that situation, there could be a library of just types we can all share.

There are also situations where we need to go down to the platform layer to properly get a type, 
and the [Locale] and [Currency] types are examples of that.

There are other things I envision for this library, such as [Duration] style shortcuts for types like [Distance].

Feel free to fork it and make improvements, I'll keep up as best I can.

# Features

* Currently there are type used for Location. More will be added as needed. 

# Installation
![Maven Central Version](https://img.shields.io/maven-central/v/io.github.aughtone/framework-types?style=flat)
```gradle
implementation("io.github.aughtone:types:${version}")
```

# Quick Start

You can use a Coordinate object, or a pair of Double values to generate a geohash from.
```kotlin
val coordinates = Coordinates(latitude = 20.05, longitude = -15.5)
```
```kotlin
val altitude = Altitude(meters = 150.5)
```
```kotlin
val amount = Money(value = 0.0, currency = Currency(currencyCode = "CAN", symbol = "$"))
```

# Feedback

Bugs can go into the issue tracker, but you are probably going to get faster support by creating a PR.   
