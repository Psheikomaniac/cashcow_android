package com.cashbox.domain.usecase.penalty

import com.cashbox.domain.model.Penalty
import com.cashbox.domain.repository.PenaltyRepository
import javax.inject.Inject

/**
 * Use case for updating an existing penalty.
 */
class UpdatePenaltyUseCase @Inject constructor(
    private val penaltyRepository: PenaltyRepository
) {
    /**
     * Update an existing penalty.
     * 
     * @param penalty The penalty to update
     * @return Result containing the updated penalty or an error
     */
    suspend operator fun invoke(penalty: Penalty): Result<Penalty> {
        return penaltyRepository.updatePenalty(penalty)
    }
}