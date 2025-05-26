package com.cashbox.domain.usecase.penalty

import com.cashbox.domain.model.Penalty
import com.cashbox.domain.repository.PenaltyRepository
import javax.inject.Inject

/**
 * Use case for retrieving a penalty by ID.
 */
class GetPenaltyByIdUseCase @Inject constructor(
    private val penaltyRepository: PenaltyRepository
) {
    /**
     * Get a penalty by ID.
     * 
     * @param id ID of the penalty
     * @return The penalty or null if not found
     */
    suspend operator fun invoke(id: String): Penalty? {
        return penaltyRepository.getPenaltyById(id)
    }
}