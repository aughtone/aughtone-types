import io.github.aughtone.types.math.BigDecimal
import io.github.aughtone.types.math.RoundingMode


object RoundingModeTests {
    fun main(argv: Array<String?>?) {
        // For each member of the family, make sure
        // rm == valueOf(rm.toString())

        for (rm in RoundingMode.entries) {
            if (rm !== RoundingMode.valueOf(rm.toString())) {
                throw RuntimeException(
                    "Bad roundtrip conversion of " +
                            rm.toString()
                )
            }
        }

        // Test that mapping of old integers to new values is correct
        if (RoundingMode.valueOf(BigDecimal.ROUND_CEILING) !==
            RoundingMode.CEILING
        ) {
            throw RuntimeException("Bad mapping for ROUND_CEILING")
        }

        if (RoundingMode.valueOf(BigDecimal.ROUND_DOWN) !==
            RoundingMode.DOWN
        ) {
            throw RuntimeException("Bad mapping for ROUND_DOWN")
        }

        if (RoundingMode.valueOf(BigDecimal.ROUND_FLOOR) !==
            RoundingMode.FLOOR
        ) {
            throw RuntimeException("Bad mapping for ROUND_FLOOR")
        }

        if (RoundingMode.valueOf(BigDecimal.ROUND_HALF_DOWN) !==
            RoundingMode.HALF_DOWN
        ) {
            throw RuntimeException("Bad mapping for ROUND_HALF_DOWN")
        }

        if (RoundingMode.valueOf(BigDecimal.ROUND_HALF_EVEN) !==
            RoundingMode.HALF_EVEN
        ) {
            throw RuntimeException("Bad mapping for ROUND_HALF_EVEN")
        }

        if (RoundingMode.valueOf(BigDecimal.ROUND_HALF_UP) !==
            RoundingMode.HALF_UP
        ) {
            throw RuntimeException("Bad mapping for ROUND_HALF_UP")
        }

        if (RoundingMode.valueOf(BigDecimal.ROUND_UNNECESSARY) !==
            RoundingMode.UNNECESSARY
        ) {
            throw RuntimeException("Bad mapping for ROUND_UNNECESARY")
        }
    }
}
