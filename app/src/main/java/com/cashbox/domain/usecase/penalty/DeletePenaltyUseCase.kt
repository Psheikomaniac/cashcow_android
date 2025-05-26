package com.cashbox.domain.usecase.penalty

import com.cashbox.domain.repository.PenaltyRepository
import javax.inject.Inject

/**
 * Use case for deleting a penalty.
 */
class DeletePenaltyUseCase @Inject constructor(
    private val penaltyRepository: PenaltyRepository
) {
    /**
     * Delete a penalty.
     * 
     * @param id ID of the penalty to delete
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(id: String): Result<Unit> {
        return penaltyRepository.deletePenalty(id)
    }
}