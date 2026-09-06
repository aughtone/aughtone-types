# Arbitrary-Precision Math Types

SPEC · 2026-05-22
Keywords: BigInteger behaviour contract, BigDecimal behaviour contract, what
          should divide throw, remainder versus mod sign, scale mechanics,
          rounding mode semantics, porting java.math to another language,
          non-terminating decimal expansion

## Overview
This document outlines the behavior and interface requirements for two arbitrary-precision numerical types: an integer type (`BigInt`) and a decimal type (`BigDec`). 

This specification is fundamentally derived from the behavioral contracts of Java's `BigInteger` and `BigDecimal`. The primary goal of this document is to facilitate the creation of a 100% behaviorally compatible implementation in another language (such as pure Kotlin, Swift, or Rust). 

**Requirements:**
*   This specification defines **inputs and expected outputs** only.
*   Implementation details, underlying arrays, and memory management are omitted and left to the implementer.
*   All instances of these types MUST be **immutable**. Operations must return a newly allocated instance (or a shared immutable instance like zero).

---

## 1. `BigInt` (Arbitrary-Precision Integer)

`BigInt` represents an arbitrary-precision integer mathematically equivalent to a signed infinite-width integer.

### 1.1 Initialization
*   **From String/Radix:**
    *   *Input:* String representation (e.g., `"12345"`, `"-FF"`) and an optional integer Radix (default 10).
    *   *Output:* A `BigInt` representing the mathematical integer.
*   **From Byte Array:**
    *   *Input:* A byte array representing a Two's-complement binary number in big-endian byte-order.
    *   *Output:* A `BigInt`. 
    *   *Example:* Input `[0x80]` -> Output `-128`. Input `[0x00, 0x80]` -> Output `128`.
*   **From Primitive Integer:**
    *   *Input:* A standard 64-bit integer.
    *   *Output:* A `BigInt`.

### 1.2 Arithmetic Operations
*All operations take a `BigInt` (the receiver `A`) and another `BigInt` (the argument `B`), and return a new `BigInt`.*
*   **Addition (`add`):** Returns $A + B$.
*   **Subtraction (`subtract`):** Returns $A - B$.
*   **Multiplication (`multiply`):** Returns $A \times B$.
*   **Division (`divide`):** Returns $\lfloor A / B \rfloor$ truncated towards zero.
    *   *Edge case:* If $B = 0$, must yield a division-by-zero error.
*   **Remainder (`remainder`):** Returns $A - (B \times (A \text{ divided by } B))$.
    *   *Note:* The sign of the result must match the sign of $A$.
*   **Divide and Remainder:** Returns a tuple `[Quotient, Remainder]`.

### 1.3 Modular Arithmetic
*   **Modulus (`mod`):**
    *   *Input:* Modulus $M$ (must be $> 0$).
    *   *Output:* Returns an integer $X$ such that $0 \le X < M$ and $(A - X)$ is a multiple of $M$.
    *   *Note:* Unlike `remainder`, this result is always positive.
*   **Modular Inverse (`modInverse`):**
    *   *Input:* Modulus $M$ ($M > 0$).
    *   *Output:* Returns $X$ such that $(A \times X) \pmod M = 1$.
    *   *Edge case:* Yields an arithmetic error if $A$ and $M$ are not coprime.
*   **Modular Exponentiation (`modPow`):**
    *   *Input:* Exponent $E$, Modulus $M$ ($M > 0$).
    *   *Output:* Returns $A^E \pmod M$. If $E < 0$, computes the modular inverse of $A$ and raises it to the power of $|E|$.

### 1.4 Bitwise Operations
Bitwise operations conceptually treat the `BigInt` as an infinite-length Two's-complement binary sequence. Negative numbers act as if they are prefixed with an infinite sequence of `1` bits.
*   **And, Or, Xor, Not:** Standard bitwise logic.
*   **Shifts (`shiftLeft`, `shiftRight`):**
    *   *Input:* Integer $N$.
    *   *Output:* Equivalent to multiplying (left shift) or dividing (right shift) by $2^N$ and flooring the result.
*   **Bit Manipulation (`setBit`, `clearBit`, `testBit`):**
    *   *Input:* Integer index $N \ge 0$.
    *   *Output:* `setBit` forces the $N$th bit to 1, `clearBit` to 0. `testBit` returns a boolean indicating if the $N$th bit is 1.

---

## 2. `BigDec` (Arbitrary-Precision Decimal)

`BigDec` represents an exact arbitrary-precision signed decimal number. It mathematically consists of an unscaled integer value and a 32-bit scale. The evaluated number is $\text{Unscaled} \times 10^{-\text{Scale}}$.

### 2.1 Initialization
*   **From String:** (Primary Initialization)
    *   *Input:* String representing a decimal (e.g., `"3.14"`, `"-1.23e-4"`).
    *   *Output:* A `BigDec`. (e.g., `"3.14"` yields Unscaled `314`, Scale `2`).
*   **From 64-bit Floating Point (Double):**
    *   *Input:* Floating point number.
    *   *Output:* The exact decimal representation of that floating point value.
    *   *Warning:* Input `0.1` must yield exactly the internal representation of the IEEE 754 floating point number (e.g., `0.100000000000000005551115...`), **not** `0.1`.
*   **From `BigInt` and Scale:**
    *   *Input:* A `BigInt` unscaled value, and an integer scale.
    *   *Output:* A `BigDec`.

### 2.2 Arithmetic Operations
Most operations accept an optional `Context` object specifying a precision (total number of significant digits) and a `RoundingMode` (e.g., `HALF_UP`, `DOWN`).

*   **Addition (`add`), Subtraction (`subtract`), Multiplication (`multiply`):**
    *   Computes the exact mathematical result. If a `Context` is provided, the result is rounded to the specified precision.
*   **Division (`divide`):**
    *   *Input:* Divisor $B$.
    *   *Output:* $A / B$.
    *   *Edge case:* If the result has an infinite decimal expansion (e.g., $1 / 3$) and no `Context` or explicit scale/rounding is provided, the implementation MUST yield a "Non-terminating decimal expansion" error.

### 2.3 Scaling and Manipulation
*   **Set Scale (`setScale`):**
    *   *Input:* Target scale $S$, optional `RoundingMode`.
    *   *Output:* A `BigDec` whose scale is $S$. If scaling reduces precision, a `RoundingMode` must be provided, or an error is thrown.
*   **Strip Trailing Zeros:**
    *   *Output:* Returns a numerically equivalent `BigDec` with trailing zeros removed from the unscaled value, adjusting the scale accordingly (e.g., `1.200` becomes `1.2`).

### 2.4 Comparisons
*   **Value Comparison (`compareTo`):**
    *   Compares exact mathematical values, ignoring differences in scale.
    *   *Example:* `compareTo` between `"2.0"` and `"2.00"` outputs `Equal`.
*   **Strict Equality (`equals`):**
    *   Compares both the unscaled mathematical value AND the scale.
    *   *Example:* `equals` between `"2.0"` and `"2.00"` outputs `False`.

---

## 3. Test Plan & Expected Outputs

A compatible implementation must verify against the following input/output test cases:

### `BigInt` Tests
| Input A | Operation | Input B | Expected Output | Notes |
| :--- | :--- | :--- | :--- | :--- |
| `byte[] {0x80}` | Init | | `-128` | Sign extension check. |
| `String "0"` | Init | | `0` | Underlying structure should gracefully represent absolute 0. |
| `-10` | `remainder` | `3` | `-1` | Sign follows the dividend. |
| `-10` | `mod` | `3` | `2` | Modulo always returns a positive integer strictly less than modulus. |
| `0` | `divide` | `0` | *Error* | Must safely catch division by zero. |
| `-1` | `shiftRight` | `1` | `-1` | Simulates an infinite stream of 1s in Two's complement. |
| `15` | `testBit` | `0` | `True` | 15 is binary `1111`, zeroth bit is 1. |

### `BigDec` Tests
| Input A | Operation | Input (Other) | Expected Output | Notes |
| :--- | :--- | :--- | :--- | :--- |
| `Float 0.1` | Init | | `0.1000000000000000055...` | Exact binary representation of floating point `0.1`. |
| `String "0.1"` | Init | | `0.1` | Exact string representation. |
| `"2.0"` | `equals` | `"2.00"` | `False` | Scales differ (1 vs 2). |
| `"2.0"` | `compareTo`| `"2.00"` | `Equal` (0) | Numerical values match. |
| `"1"` | `divide` | `"3"` | *Error* | Non-terminating expansion with no rounding context. |
| `"1"` | `divide` | `"3", Scale=2, HALF_UP` | `"0.33"` | Terminates cleanly based on rounding rules. |
| `"600.0"` | `stripTrailingZeros` | | `"6E+2"` | Scale is reduced appropriately, yielding `6` with scale `-2`. |
