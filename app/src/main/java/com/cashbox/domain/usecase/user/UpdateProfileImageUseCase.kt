package com.cashbox.domain.usecase.user

import com.cashbox.domain.model.User
import com.cashbox.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use case for updating the user's profile image.
 */
class UpdateProfileImageUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * Update the user's profile image.
     * 
     * @param imageUri URI of the image
     * @return Result containing the updated user or an error
     */
    suspend operator fun invoke(imageUri: String): Result<User> {
        return userRepository.updateProfileImage(imageUri)
    }
}