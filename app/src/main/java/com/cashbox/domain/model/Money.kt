package com.cashbox.domain.model

/**
 * Value class representing money amount in cents.
 */
@JvmInline
value class Money(private val cents: Int) {
    val amount: Int get() = cents
    
    fun toCurrency(currency: Currency = Currency.EUR): String {
        val amount = cents / 100.0
        return when (currency) {
            Currency.EUR -> "%.2f €".format(amount)
            Currency.USD -> "$%.2f".format(amount)
            Currency.GBP -> "£%.2f".format(amount)
        }
    }
    
    companion object {
        fun fromCents(cents: Int) = Money(cents)
        fun fromAmount(amount: Double) = Money((amount * 100).toInt())
    }
}

/**
 * Enum representing supported currencies.
 */
enum class Currency {
    EUR,
    USD,
    GBP
}