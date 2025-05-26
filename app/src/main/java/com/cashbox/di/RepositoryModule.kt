package com.cashbox.di

import com.cashbox.data.repository.PenaltyRepositoryImpl
import com.cashbox.domain.repository.PenaltyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing repository implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds the PenaltyRepositoryImpl implementation to the PenaltyRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindPenaltyRepository(
        penaltyRepositoryImpl: PenaltyRepositoryImpl
    ): PenaltyRepository
}