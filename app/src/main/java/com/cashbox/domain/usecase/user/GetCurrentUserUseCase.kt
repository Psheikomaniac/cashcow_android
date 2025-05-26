package com.cashbox.domain.usecase.user

import com.cashbox.domain.model.User
import com.cashbox.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving the current user.
 */
class GetCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * Get the current user.
     * 
     * @return Flow of the current user or null if not logged in
     */
    operator fun invoke(): Flow<User?> {
        return userRepository.getCurrentUser()
    }
}