package com.cashbox.domain.repository

import com.cashbox.domain.model.Team
import com.cashbox.domain.model.TeamUser
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing teams.
 */
interface TeamRepository {
    
    /**
     * Get all teams for the current user.
     * 
     * @return Flow of list of teams
     */
    fun getUserTeams(): Flow<List<Team>>
    
    /**
     * Get a team by ID.
     * 
     * @param id ID of the team
     * @return The team or null if not found
     */
    suspend fun getTeamById(id: String): Team?
    
    /**
     * Create a new team.
     * 
     * @param team The team to create
     * @return Result containing the created team or an error
     */
    suspend fun createTeam(team: Team): Result<Team>
    
    /**
     * Update an existing team.
     * 
     * @param team The team to update
     * @return Result containing the updated team or an error
     */
    suspend fun updateTeam(team: Team): Result<Team>
    
    /**
     * Delete a team.
     * 
     * @param id ID of the team to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteTeam(id: String): Result<Unit>
    
    /**
     * Get all members of a team.
     * 
     * @param teamId ID of the team
     * @return Flow of list of team users
     */
    fun getTeamMembers(teamId: String): Flow<List<TeamUser>>
    
    /**
     * Add a user to a team.
     * 
     * @param teamId ID of the team
     * @param email Email of the user to add
     * @param role Role of the user in the team
     * @return Result containing the added team user or an error
     */
    suspend fun addTeamMember(teamId: String, email: String, role: com.cashbox.domain.model.TeamRole): Result<TeamUser>
    
    /**
     * Remove a user from a team.
     * 
     * @param teamUserId ID of the team user to remove
     * @return Result indicating success or failure
     */
    suspend fun removeTeamMember(teamUserId: String): Result<Unit>
    
    /**
     * Update a team member's role.
     * 
     * @param teamUserId ID of the team user
     * @param role New role for the team user
     * @return Result containing the updated team user or an error
     */
    suspend fun updateTeamMemberRole(teamUserId: String, role: com.cashbox.domain.model.TeamRole): Result<TeamUser>
}