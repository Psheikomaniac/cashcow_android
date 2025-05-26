package com.cashbox.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cashbox.data.local.converters.DateConverters
import com.cashbox.data.local.dao.PenaltyDao
import com.cashbox.data.local.entities.PenaltyEntity

@Database(
    entities = [
        PenaltyEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class CashboxDatabase : RoomDatabase() {
    
    abstract fun penaltyDao(): PenaltyDao
    
    // Add more DAOs as needed
}