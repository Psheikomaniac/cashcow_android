package com.cashbox.domain.usecase.penalty

import com.cashbox.domain.model.Penalty
import com.cashbox.domain.repository.PenaltyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving all penalties.
 */
class GetPenaltiesUseCase @Inject constructor(
    private val penaltyRepository: PenaltyRepository
) {
    /**
     * Get all penalties.
     * 
     * @return Flow of list of penalties
     */
    operator fun invoke(): Flow<List<Penalty>> {
        return penaltyRepository.getAllPenalties()
    }
}