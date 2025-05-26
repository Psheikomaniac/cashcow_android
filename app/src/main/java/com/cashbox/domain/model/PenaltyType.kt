package com.cashbox.domain.model

/**
 * Domain model representing a penalty type.
 */
data class PenaltyType(
    val id: String,
    val name: String,
    val description: String?,
    val type: PenaltyTypeCategory,
    val active: Boolean = true
)

/**
 * Enum representing the category of a penalty type.
 */
enum class PenaltyTypeCategory {
    DRINK,
    LATE_ARRIVAL,
    MISSED_TRAINING,
    CUSTOM;
    
    fun getDisplayName(): String = when (this) {
        DRINK -> "Drink"
        LATE_ARRIVAL -> "Late Arrival"
        MISSED_TRAINING -> "Missed Training"
        CUSTOM -> "Custom"
    }
    
    fun isDrink(): Boolean = this == DRINK
}