package com.cashbox.domain.usecase.team

import com.cashbox.domain.model.TeamRole
import com.cashbox.domain.model.TeamUser
import com.cashbox.domain.repository.TeamRepository
import javax.inject.Inject

/**
 * Use case for adding a member to a team.
 */
class AddTeamMemberUseCase @Inject constructor(
    private val teamRepository: TeamRepository
) {
    /**
     * Add a user to a team.
     * 
     * @param teamId ID of the team
     * @param email Email of the user to add
     * @param role Role of the user in the team
     * @return Result containing the added team user or an error
     */
    suspend operator fun invoke(teamId: String, email: String, role: TeamRole): Result<TeamUser> {
        return teamRepository.addTeamMember(teamId, email, role)
    }
}