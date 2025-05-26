package com.cashbox.domain.usecase.team

import com.cashbox.domain.model.Team
import com.cashbox.domain.repository.TeamRepository
import javax.inject.Inject

/**
 * Use case for updating an existing team.
 */
class UpdateTeamUseCase @Inject constructor(
    private val teamRepository: TeamRepository
) {
    /**
     * Update an existing team.
     * 
     * @param team The team to update
     * @return Result containing the updated team or an error
     */
    suspend operator fun invoke(team: Team): Result<Team> {
        return teamRepository.updateTeam(team)
    }
}