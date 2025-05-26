package com.cashbox.data.repository

import com.cashbox.domain.model.Penalty
import com.cashbox.domain.repository.PenaltyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [PenaltyRepository] that provides a temporary mock implementation.
 * This will be replaced with a real implementation that uses the local database and remote API.
 */
@Singleton
class PenaltyRepositoryImpl @Inject constructor() : PenaltyRepository {
    
    // Temporary empty list for mock implementation
    private val emptyPenaltyList = emptyList<Penalty>()
    
    override fun getAllPenalties(): Flow<List<Penalty>> {
        return flowOf(emptyPenaltyList)
    }
    
    override suspend fun getPenaltyById(id: String): Penalty? {
        return null
    }
    
    override suspend fun createPenalty(penalty: Penalty): Result<Penalty> {
        return Result.success(penalty)
    }
    
    override suspend fun updatePenalty(penalty: Penalty): Result<Penalty> {
        return Result.success(penalty)
    }
    
    override suspend fun deletePenalty(id: String): Result<Unit> {
        return Result.success(Unit)
    }
    
    override suspend fun markPenaltyAsPaid(id: String): Result<Penalty> {
        return Result.failure(NotImplementedError("Mock implementation"))
    }
}