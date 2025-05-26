package com.cashbox.domain.usecase.team

import com.cashbox.domain.model.Team
import com.cashbox.domain.repository.TeamRepository
import javax.inject.Inject

/**
 * Use case for retrieving a team by ID.
 */
class GetTeamByIdUseCase @Inject constructor(
    private val teamRepository: TeamRepository
) {
    /**
     * Get a team by ID.
     * 
     * @param id ID of the team
     * @return The team or null if not found
     */
    suspend operator fun invoke(id: String): Team? {
        return teamRepository.getTeamById(id)
    }
}