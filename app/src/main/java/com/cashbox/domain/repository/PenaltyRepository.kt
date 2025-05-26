package com.cashbox.domain.repository

import com.cashbox.domain.model.Penalty
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing penalties.
 */
interface PenaltyRepository {
    /**
     * Get all penalties.
     * 
     * @return Flow of list of penalties
     */
    fun getAllPenalties(): Flow<List<Penalty>>
    
    /**
     * Get a penalty by ID.
     * 
     * @param id ID of the penalty
     * @return The penalty or null if not found
     */
    suspend fun getPenaltyById(id: String): Penalty?
    
    /**
     * Create a new penalty.
     * 
     * @param penalty The penalty to create
     * @return Result containing the created penalty or an exception
     */
    suspend fun createPenalty(penalty: Penalty): Result<Penalty>
    
    /**
     * Update an existing penalty.
     * 
     * @param penalty The penalty to update
     * @return Result containing the updated penalty or an exception
     */
    suspend fun updatePenalty(penalty: Penalty): Result<Penalty>
    
    /**
     * Delete a penalty.
     * 
     * @param id ID of the penalty to delete
     * @return Result containing success or an exception
     */
    suspend fun deletePenalty(id: String): Result<Unit>
    
    /**
     * Mark a penalty as paid.
     * 
     * @param id ID of the penalty to mark as paid
     * @return Result containing the updated penalty or an exception
     */
    suspend fun markPenaltyAsPaid(id: String): Result<Penalty>
}