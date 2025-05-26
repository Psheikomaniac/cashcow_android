package com.cashbox.domain.usecase.team

import com.cashbox.domain.model.Team
import com.cashbox.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving all teams for the current user.
 */
class GetUserTeamsUseCase @Inject constructor(
    private val teamRepository: TeamRepository
) {
    /**
     * Get all teams for the current user.
     * 
     * @return Flow of list of teams
     */
    operator fun invoke(): Flow<List<Team>> {
        return teamRepository.getUserTeams()
    }
}