package com.cashbox.domain.usecase.team

import com.cashbox.domain.model.TeamRole
import com.cashbox.domain.model.TeamUser
import com.cashbox.domain.repository.TeamRepository
import javax.inject.Inject

/**
 * Use case for updating a team member's role.
 */
class UpdateTeamMemberRoleUseCase @Inject constructor(
    private val teamRepository: TeamRepository
) {
    /**
     * Update a team member's role.
     * 
     * @param teamUserId ID of the team user
     * @param role New role for the team user
     * @return Result containing the updated team user or an error
     */
    suspend operator fun invoke(teamUserId: String, role: TeamRole): Result<TeamUser> {
        return teamRepository.updateTeamMemberRole(teamUserId, role)
    }
}