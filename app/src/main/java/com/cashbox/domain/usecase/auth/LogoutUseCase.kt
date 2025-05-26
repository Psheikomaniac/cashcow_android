package com.cashbox.domain.usecase.auth

import com.cashbox.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for user logout.
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Log out the current user.
     * 
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}