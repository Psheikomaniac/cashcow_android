package com.cashbox.domain.repository

import com.cashbox.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing users.
 */
interface UserRepository {
    
    /**
     * Get the current user.
     * 
     * @return Flow of the current user or null if not logged in
     */
    fun getCurrentUser(): Flow<User?>
    
    /**
     * Get a user by ID.
     * 
     * @param id ID of the user
     * @return The user or null if not found
     */
    suspend fun getUserById(id: String): User?
    
    /**
     * Update the current user's profile.
     * 
     * @param user The updated user information
     * @return Result containing the updated user or an error
     */
    suspend fun updateUserProfile(user: User): Result<User>
    
    /**
     * Update the user's profile image.
     * 
     * @param imageUri URI of the image
     * @return Result containing the updated user or an error
     */
    suspend fun updateProfileImage(imageUri: String): Result<User>
    
    /**
     * Log out the current user.
     * 
     * @return Result indicating success or failure
     */
    suspend fun logout(): Result<Unit>
}