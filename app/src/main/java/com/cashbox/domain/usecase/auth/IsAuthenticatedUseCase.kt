package com.cashbox.domain.usecase.auth

import com.cashbox.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for checking if the user is authenticated.
 */
class IsAuthenticatedUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Check if the user is authenticated.
     * 
     * @return True if the user is authenticated, false otherwise
     */
    operator fun invoke(): Boolean {
        return authRepository.isAuthenticated()
    }
}