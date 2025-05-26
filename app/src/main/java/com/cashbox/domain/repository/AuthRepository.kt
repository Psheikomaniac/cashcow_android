package com.cashbox.domain.repository

import com.cashbox.domain.model.User

/**
 * Repository interface for authentication and authorization.
 */
interface AuthRepository {
    
    /**
     * Login with email and password.
     * 
     * @param email User's email
     * @param password User's password
     * @return Result containing the logged-in user or an error
     */
    suspend fun login(email: String, password: String): Result<User>
    
    /**
     * Register a new user.
     * 
     * @param name User's name
     * @param email User's email
     * @param password User's password
     * @return Result containing the registered user or an error
     */
    suspend fun register(name: String, email: String, password: String): Result<User>
    
    /**
     * Request a password reset.
     * 
     * @param email User's email
     * @return Result indicating success or failure
     */
    suspend fun requestPasswordReset(email: String): Result<Unit>
    
    /**
     * Verify if the user is authenticated.
     * 
     * @return True if the user is authenticated, false otherwise
     */
    fun isAuthenticated(): Boolean
    
    /**
     * Refresh the authentication token.
     * 
     * @return Result indicating success or failure
     */
    suspend fun refreshToken(): Result<Unit>
    
    /**
     * Authenticate with biometrics.
     * 
     * @return Result containing the logged-in user or an error
     */
    suspend fun authenticateWithBiometrics(): Result<User>
}