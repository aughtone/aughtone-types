# AC 0001: Arbitrary-Precision Math Types

**Requirement Link**: [0001-arbitrary-precision-math.md](../prd/0001-arbitrary-precision-math.md)

## Scenarios: BigInt

**Scenario: Sign Extension Initialization**
Given a `BigInt` initialized from byte array `[0x80]`
Then the resulting mathematical value is `-128`

**Scenario: Remainder sign follows dividend**
Given `BigInt` A is `-10`
And `BigInt` B is `3`
When `remainder` is invoked
Then the result is `-1`

**Scenario: Modulo strictly yields positive relative range**
Given `BigInt` A is `-10`
And `BigInt` B is `3`
When `mod` is invoked
Then the result is `2`

**Scenario: Division by Zero Safety**
Given `BigInt` A is `0`
And `BigInt` B is `0`
When `divide` is invoked
Then a division-by-zero error is safely thrown

**Scenario: Infinite stream of 1s in Two's Complement**
Given `BigInt` A is `-1`
And shift is `1`
When `shiftRight` is invoked
Then the result remains `-1`

**Scenario: Bit manipulation lookup**
Given `BigInt` A is `15` (binary 1111)
When `testBit(0)` is invoked
Then the result is `True`

## Scenarios: BigDec

**Scenario: Exact binary representation from Double**
Given a `BigDec` initialized from Float `0.1`
Then the internal representation strictly captures `0.1000000000000000055...` instead of a truncated `0.1`

**Scenario: Strict Equality (Value + Scale)**
Given `BigDec` A is `"2.0"`
And `BigDec` B is `"2.00"`
When `equals` is invoked
Then the result is `False`

**Scenario: Numerical Value Comparison**
Given `BigDec` A is `"2.0"`
And `BigDec` B is `"2.00"`
When `compareTo` is invoked
Then the result is `Equal` (0)

**Scenario: Non-terminating decimal expansion guard**
Given `BigDec` A is `"1"`
And `BigDec` B is `"3"`
When `divide` is invoked without a rounding context
Then a non-terminating decimal expansion error is thrown

**Scenario: Context-based rounding termination**
Given `BigDec` A is `"1"`
And `BigDec` B is `"3"`
When `divide` is invoked with Scale=2 and RoundingMode=HALF_UP
Then the result is `"0.33"`

**Scenario: Stripping Trailing Zeros**
Given `BigDec` A is `"600.0"`
When `stripTrailingZeros` is invoked
Then the result has its scale reduced appropriately (yielding unscaled `6`, scale `-2`, typically represented as `6E+2`)

## Scenarios: Comparison & Differential Testing

**Scenario: KMP Differential Parity**
Given a generated suite of test values (including edge cases, zero, and random integers/decimals)
When operations (addition, subtraction, multiplication, division, modulo, shifts, scale adjustment, rounding, and parsing) are executed on both `io.github.aughtone.types.number` and `com.ionspin.kotlin.bignum`
Then the string values, comparisons, scales, and outputs must exactly match.

**Scenario: JVM Baseline Parity**
Given a generated suite of test values (including edge cases, zero, and random integers/decimals)
When operations (addition, subtraction, multiplication, division, modulo, shifts, scale adjustment, rounding, and parsing) are executed on both `io.github.aughtone.types.number` and JDK standard library math types (`java.math.BigInteger`/`BigDecimal`)
Then the string values, comparisons, scales, and outputs must exactly match.

