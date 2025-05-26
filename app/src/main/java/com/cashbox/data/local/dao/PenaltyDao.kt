package com.cashbox.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cashbox.data.local.entities.PenaltyEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the penalties table.
 */
@Dao
interface PenaltyDao {
    
    @Query("SELECT * FROM penalties WHERE archived = 0")
    fun getAllActivePenalties(): Flow<List<PenaltyEntity>>
    
    @Query("SELECT * FROM penalties WHERE teamUserId = :userId")
    fun getPenaltiesByUser(userId: String): Flow<List<PenaltyEntity>>
    
    @Query("SELECT * FROM penalties WHERE id = :id")
    suspend fun getPenaltyById(id: String): PenaltyEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPenalty(penalty: PenaltyEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPenalties(penalties: List<PenaltyEntity>)
    
    @Update
    suspend fun updatePenalty(penalty: PenaltyEntity)
    
    @Delete
    suspend fun deletePenalty(penalty: PenaltyEntity)
    
    @Query("DELETE FROM penalties WHERE id = :id")
    suspend fun deletePenaltyById(id: String)
    
    @Query("UPDATE penalties SET paidAt = :paidAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markPenaltyAsPaid(id: String, paidAt: Long, updatedAt: Long)
    
    @Query("SELECT * FROM penalties WHERE syncStatus = :syncStatus")
    suspend fun getPenaltiesBySyncStatus(syncStatus: String): List<PenaltyEntity>
}