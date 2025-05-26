package com.cashbox.data.local.converters

import androidx.room.TypeConverter
import java.time.Instant

/**
 * Type converters for Room database to convert between database types and domain types.
 */
class DateConverters {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }
    
    @TypeConverter
    fun toTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }
}