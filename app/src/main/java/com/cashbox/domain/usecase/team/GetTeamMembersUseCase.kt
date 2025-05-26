package com.cashbox.domain.usecase.team

import com.cashbox.domain.model.TeamUser
import com.cashbox.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving all members of a team.
 */
class GetTeamMembersUseCase @Inject constructor(
    private val teamRepository: TeamRepository
) {
    /**
     * Get all members of a team.
     * 
     * @param teamId ID of the team
     * @return Flow of list of team users
     */
    operator fun invoke(teamId: String): Flow<List<TeamUser>> {
        return teamRepository.getTeamMembers(teamId)
    }
}