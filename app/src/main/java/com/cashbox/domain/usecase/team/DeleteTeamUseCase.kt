package com.cashbox.domain.usecase.team

import com.cashbox.domain.repository.TeamRepository
import javax.inject.Inject

/**
 * Use case for deleting a team.
 */
class DeleteTeamUseCase @Inject constructor(
    private val teamRepository: TeamRepository
) {
    /**
     * Delete a team.
     * 
     * @param id ID of the team to delete
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(id: String): Result<Unit> {
        return teamRepository.deleteTeam(id)
    }
}