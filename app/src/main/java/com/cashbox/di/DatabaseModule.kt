package com.cashbox.di

import android.content.Context
import androidx.room.Room
import com.cashbox.data.local.dao.PenaltyDao
import com.cashbox.data.local.database.CashboxDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCashboxDatabase(
        @ApplicationContext context: Context
    ): CashboxDatabase {
        return Room.databaseBuilder(
            context,
            CashboxDatabase::class.java,
            "cashbox.db"
        )
        .fallbackToDestructiveMigration() // For development only, remove in production
        .build()
    }

    @Provides
    @Singleton
    fun providePenaltyDao(database: CashboxDatabase): PenaltyDao {
        return database.penaltyDao()
    }
}
