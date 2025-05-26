package com.cashbox.domain.usecase.auth

import com.cashbox.domain.model.User
import com.cashbox.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for user login.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Login with email and password.
     * 
     * @param email User's email
     * @param password User's password
     * @return Result containing the logged-in user or an error
     */
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return authRepository.login(email, password)
    }
}