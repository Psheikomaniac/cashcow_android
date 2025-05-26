package com.cashbox.domain.model

/**
 * Domain model representing a user in a team.
 */
data class TeamUser(
    val id: String,
    val userId: String,
    val teamId: String,
    val name: String,
    val email: String,
    val role: TeamRole,
    val active: Boolean = true
)

/**
 * Enum representing the role of a user in a team.
 */
enum class TeamRole {
    ADMIN,
    MEMBER,
    GUEST;
    
    fun getDisplayName(): String = when (this) {
        ADMIN -> "Admin"
        MEMBER -> "Member"
        GUEST -> "Guest"
    }
    
    fun canManagePenalties(): Boolean = this == ADMIN
}