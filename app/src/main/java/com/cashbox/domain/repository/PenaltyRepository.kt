package com.cashbox.domain.repository

import com.cashbox.domain.model.Penalty
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Repository interface for managing penalties.
 */
interface PenaltyRepository {
    
    /**
     * Get all active penalties.
     * 
     * @return Flow of list of penalties
     */
    fun getAllPenalties(): Flow<List<Penalty>>
    
    /**
     * Get penalties for a specific user.
     * 
     * @param userId ID of the user
     * @return Flow of list of penalties
     */
    fun getPenaltiesByUser(userId: String): Flow<List<Penalty>>
    
    /**
     * Get a penalty by its ID.
     * 
     * @param id ID of the penalty
     * @return The penalty or null if not found
     */
    suspend fun getPenaltyById(id: String): Penalty?
    
    /**
     * Create a new penalty.
     * 
     * @param penalty The penalty to create
     * @return Result containing the created penalty or an error
     */
    suspend fun createPenalty(penalty: Penalty): Result<Penalty>
    
    /**
     * Update an existing penalty.
     * 
     * @param penalty The penalty to update
     * @return Result containing the updated penalty or an error
     */
    suspend fun updatePenalty(penalty: Penalty): Result<Penalty>
    
    /**
     * Delete a penalty.
     * 
     * @param id ID of the penalty to delete
     * @return Result indicating success or failure
     */
    suspend fun deletePenalty(id: String): Result<Unit>
    
    /**
     * Mark a penalty as paid.
     * 
     * @param id ID of the penalty
     * @param paidAt Timestamp when the penalty was paid
     * @return Result containing the updated penalty or an error
     */
    suspend fun markPenaltyAsPaid(id: String, paidAt: Instant = Instant.now()): Result<Penalty>
    
    /**
     * Synchronize penalties with the remote server.
     * 
     * @return Result indicating success or failure
     */
    suspend fun syncPenalties(): Result<Unit>
}