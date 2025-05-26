package com.cashbox.domain.model

/**
 * Domain model representing a team.
 */
data class Team(
    val id: String,
    val name: String,
    val description: String? = null,
    val logoUrl: String? = null,
    val createdAt: java.time.Instant,
    val updatedAt: java.time.Instant
)