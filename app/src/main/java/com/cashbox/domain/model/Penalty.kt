package com.cashbox.domain.model

import java.time.Instant

/**
 * Domain model representing a penalty.
 */
data class Penalty(
    val id: String,
    val teamUser: TeamUser,
    val type: PenaltyType,
    val reason: String,
    val amount: Money,
    val archived: Boolean = false,
    val paidAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val isPaid: Boolean
        get() = paidAt != null
        
    fun toPaid(paidAt: Instant = Instant.now()): Penalty {
        return copy(
            paidAt = paidAt,
            updatedAt = Instant.now()
        )
    }
    
    fun toArchived(): Penalty {
        return copy(
            archived = true,
            updatedAt = Instant.now()
        )
    }
}