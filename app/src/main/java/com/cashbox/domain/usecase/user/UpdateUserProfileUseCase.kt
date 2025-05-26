package com.cashbox.domain.usecase.user

import com.cashbox.domain.model.User
import com.cashbox.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use case for updating the current user's profile.
 */
class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * Update the current user's profile.
     * 
     * @param user The updated user information
     * @return Result containing the updated user or an error
     */
    suspend operator fun invoke(user: User): Result<User> {
        return userRepository.updateUserProfile(user)
    }
}