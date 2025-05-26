package com.cashbox.domain.model

/**
 * Domain model representing a user.
 */
data class User(
    val id: String,
    val email: String,
    val name: String,
    val profileImageUrl: String? = null,
    val isEmailVerified: Boolean = false
)