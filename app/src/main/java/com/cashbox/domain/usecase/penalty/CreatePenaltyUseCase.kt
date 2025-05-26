package com.cashbox.domain.usecase.penalty

import com.cashbox.domain.model.Penalty
import com.cashbox.domain.repository.PenaltyRepository
import javax.inject.Inject

/**
 * Use case for creating a new penalty.
 */
class CreatePenaltyUseCase @Inject constructor(
    private val penaltyRepository: PenaltyRepository
) {
    /**
     * Create a new penalty.
     * 
     * @param penalty The penalty to create
     * @return Result containing the created penalty or an error
     */
    suspend operator fun invoke(penalty: Penalty): Result<Penalty> {
        return penaltyRepository.createPenalty(penalty)
    }
}