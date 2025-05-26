package com.cashbox.domain.usecase.user

import com.cashbox.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use case for logging out the current user.
 */
class LogoutUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * Log out the current user.
     * 
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(): Result<Unit> {
        return userRepository.logout()
    }
}