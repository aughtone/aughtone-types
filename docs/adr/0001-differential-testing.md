# ADR 0001: Differential Testing for Arbitrary-Precision Math Types

## Status
Accepted

## Context
Standard arbitrary-precision math types (`BigInteger` and `BigDecimal`) have complex behavior regarding exact values, parsing, scaling, shifts, modular arithmetic, and rounding. Testing only simple scenarios (e.g. `2 + 2 = 4`) does not guarantee parity across a massive range of numerical coordinates.

We need a way to robustly verify that our pure-Kotlin implementation matches the expected mathematical behavior of well-established, baseline arbitrary-precision libraries.

## Decision
We will implement **Differential Parity Testing**:
1. **JDK Math Baseline**: Since JDK's `java.math.BigInteger` and `java.math.BigDecimal` are highly optimized and standard reference implementations, they will serve as our primary baseline on the JVM platform.
2. **KMP Math Baseline**: For non-JVM platforms, we will compare against the official `com.ionspin.kotlin:bignum` library.
3. **Dependency Isolation**: These external math packages will only be declared as `commonTest` dependencies in the version catalog (`libs.bignum`). They will not be referenced or depended on in any shape or form by the published library (`commonMain`).

## Consequences
- Testing complexity will be slightly higher as we maintain test suites referencing separate libraries.
- The test coverage for mathematical operations will cover edge cases, random ranges, rounding boundaries, and parsing behaviors, yielding extremely high confidence in the correctness of our implementation.
- Parity is verified continuously during Gradle test execution on all platforms.

## Baseline Library Quirks & Known Issues

During the implementation of differential parity testing, we identified several functional limitations and bugs in the Ionspin KMP `bignum` library (`com.ionspin.kotlin:bignum`). To prevent test suite instability or false positives, the parity tests implement specific workarounds:

1. **Negative Shifts**: Ionspin's `BigInteger.shl` and `shr` do not support negative shift counts, throwing an `IllegalArgumentException`. We map negative shifts safely to their opposite shift operators in our tests.
2. **Bitwise Transitions to Zero**: Ionspin bitwise operations (such as `not` and `setBitAt`) throw a constructor sign error if the operation results in a magnitude of `0` (e.g. `x.not()` on negative numbers or certain bit toggles). These scenarios are bypassed in KMP tests.
3. **Remainder Operator Sign Bug**: Ionspin's `%`/`remainder` operator has a sign bug where it returns the sign of the quotient instead of the dividend. Parity tests against Ionspin skip negative operand remainder scenarios.
4. **BigDecimal Scale 0 Representation**: Ionspin formats scale 0 decimals with a trailing `.0` suffix (e.g. returning `"1.0"` instead of `"1"`). Parity checks are performed using exact value comparison (`compareTo`) rather than string comparison.
