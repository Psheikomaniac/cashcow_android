package com.cashbox.domain.usecase.team

import com.cashbox.domain.model.Team
import com.cashbox.domain.repository.TeamRepository
import javax.inject.Inject

/**
 * Use case for creating a new team.
 */
class CreateTeamUseCase @Inject constructor(
    private val teamRepository: TeamRepository
) {
    /**
     * Create a new team.
     * 
     * @param team The team to create
     * @return Result containing the created team or an error
     */
    suspend operator fun invoke(team: Team): Result<Team> {
        return teamRepository.createTeam(team)
    }
}