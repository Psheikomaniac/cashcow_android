package com.cashbox.domain.usecase.auth

import com.cashbox.domain.model.User
import com.cashbox.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for authenticating with biometrics.
 */
class AuthenticateWithBiometricsUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Authenticate with biometrics.
     * 
     * @return Result containing the logged-in user or an error
     */
    suspend operator fun invoke(): Result<User> {
        return authRepository.authenticateWithBiometrics()
    }
}