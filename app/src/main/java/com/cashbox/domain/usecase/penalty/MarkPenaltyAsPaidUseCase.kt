package com.cashbox.domain.usecase.penalty

import com.cashbox.domain.model.Penalty
import com.cashbox.domain.repository.PenaltyRepository
import java.time.Instant
import javax.inject.Inject

/**
 * Use case for marking a penalty as paid.
 */
class MarkPenaltyAsPaidUseCase @Inject constructor(
    private val penaltyRepository: PenaltyRepository
) {
    /**
     * Mark a penalty as paid.
     * 
     * @param id ID of the penalty
     * @param paidAt Timestamp when the penalty was paid
     * @return Result containing the updated penalty or an error
     */
    suspend operator fun invoke(id: String, paidAt: Instant = Instant.now()): Result<Penalty> {
        // Note: paidAt is captured but not used in the repository call as the API handles the timestamp
        return penaltyRepository.markPenaltyAsPaid(id)
    }
}
