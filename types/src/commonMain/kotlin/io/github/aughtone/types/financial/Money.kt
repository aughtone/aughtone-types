package io.github.aughtone.types.financial

import kotlinx.serialization.Serializable


@Serializable
data class Money(
    val value: Double,
    val currency: Currency,
) {
    companion object {
        fun zero(currency: Currency) =
            Money(0.0, currency)
    }
}
