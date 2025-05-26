package com.cashbox.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for penalties table.
 */
@Entity(tableName = "penalties")
data class PenaltyEntity(
    @PrimaryKey val id: String,
    val teamUserId: String,
    val typeId: String,
    val reason: String,
    val amount: Int,
    val currency: String,
    val archived: Boolean = false,
    val paidAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

/**
 * Enum representing the synchronization status of an entity.
 */
enum class SyncStatus {
    SYNCED,     // Entity is synchronized with the server
    PENDING,    // Entity is pending synchronization with the server
    ERROR       // Error occurred during synchronization
}