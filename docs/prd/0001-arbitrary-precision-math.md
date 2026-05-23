# PRD 0001: Arbitrary-Precision Math Types (`BigInt` & `BigDec`)

## Problem Statement
Kotlin Multiplatform lacks a unified, cross-platform standard library equivalent for Java's `BigInteger` and `BigDecimal`. Developers building multiplatform applications require robust, behaviorally consistent arbitrary-precision math types to handle massive numbers and precise financial/scientific calculations without platform-specific implementations or floating-point precision loss.

## User Stories
1. **As a developer**, I want a `BigInt` type so that I can compute with arbitrary-precision signed integers mathematically equivalent to infinite-width integers without overflow.
2. **As a developer**, I want to initialize `BigInt` from Strings (with optional radix), two's complement big-endian byte arrays, and standard 64-bit primitives so that I can easily port external data.
3. **As a developer**, I want to perform standard arithmetic (`add`, `subtract`, `multiply`, `divide`, `remainder`, and divide/remainder tuples) on `BigInt` to support core mathematical operations.
4. **As a developer**, I want to execute modular arithmetic (`mod`, `modInverse`, `modPow`) on `BigInt` to support cryptographic and advanced numerical logic.
5. **As a developer**, I want to perform bitwise logic (And, Or, Xor, Not) and shifts on `BigInt` as if it were an infinite two's complement binary sequence to support bit-level manipulation.
6. **As a developer**, I want to test, set, and clear specific bits on a `BigInt` by index.
7. **As a developer**, I want a `BigDec` type (composed of an unscaled arbitrary-precision integer and a 32-bit scale) so that I can perform exact decimal arithmetic.
8. **As a developer**, I want to initialize `BigDec` from Strings, 64-bit floating point numbers (capturing their exact binary representation), and directly from a `BigInt` and scale.
9. **As a developer**, I want to perform exact decimal arithmetic, utilizing an optional `Context` (precision and `RoundingMode`) to strictly control precision loss.
10. **As a developer**, I expect a strict error to be thrown on non-terminating decimal expansions during division if no context is provided, to prevent silent infinite loops or precision truncation.
11. **As a developer**, I want to accurately compare the numerical value of two `BigDec`s (`compareTo`) regardless of their internal scales.
12. **As a developer**, I want to strictly compare equality of two `BigDec`s (`equals`), enforcing that both the value and the scale exactly match.

## Implementation Decisions
- **Immutability Contract**: All `BigInt` and `BigDec` instances MUST be completely immutable. Every mathematical operation must yield a newly allocated instance (or return a shared immutable instance like zero).
- **Architecture**: The types will define common interfaces/contracts in `commonMain` while allowing for optimized platform-specific implementations (e.g., delegating to `java.math.BigInteger` on JVM).
- **Scale Mechanics**: `BigDec` mathematically represents unscaled * 10^-scale.

## Verification
- **AC Link**: [0001-arbitrary-precision-math.md](../ac/0001-arbitrary-precision-math.md)
