package com.cashbox.domain.usecase.auth

import com.cashbox.domain.model.User
import com.cashbox.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for user registration.
 */
class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Register a new user.
     * 
     * @param name User's name
     * @param email User's email
     * @param password User's password
     * @return Result containing the registered user or an error
     */
    suspend operator fun invoke(name: String, email: String, password: String): Result<User> {
        return authRepository.register(name, email, password)
    }
}