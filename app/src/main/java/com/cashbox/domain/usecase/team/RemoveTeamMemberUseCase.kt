package com.cashbox.domain.usecase.team

import com.cashbox.domain.repository.TeamRepository
import javax.inject.Inject

/**
 * Use case for removing a member from a team.
 */
class RemoveTeamMemberUseCase @Inject constructor(
    private val teamRepository: TeamRepository
) {
    /**
     * Remove a user from a team.
     * 
     * @param teamUserId ID of the team user to remove
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(teamUserId: String): Result<Unit> {
        return teamRepository.removeTeamMember(teamUserId)
    }
}