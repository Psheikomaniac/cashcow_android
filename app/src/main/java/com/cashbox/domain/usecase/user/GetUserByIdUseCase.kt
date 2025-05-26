package com.cashbox.domain.usecase.user

import com.cashbox.domain.model.User
import com.cashbox.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use case for retrieving a user by ID.
 */
class GetUserByIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * Get a user by ID.
     * 
     * @param id ID of the user
     * @return The user or null if not found
     */
    suspend operator fun invoke(id: String): User? {
        return userRepository.getUserById(id)
    }
}