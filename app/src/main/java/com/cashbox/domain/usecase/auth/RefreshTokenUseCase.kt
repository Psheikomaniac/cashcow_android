package com.cashbox.domain.usecase.auth

import com.cashbox.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for refreshing the authentication token.
 */
class RefreshTokenUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Refresh the authentication token.
     * 
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.refreshToken()
    }
}