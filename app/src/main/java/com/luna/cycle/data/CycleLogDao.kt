package com.luna.cycle.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleLogDao {
    @Query("SELECT * FROM cycle_logs ORDER BY date DESC")
    fun observeAll(): Flow<List<CycleLog>>

    @Query("SELECT * FROM cycle_logs WHERE date = :date LIMIT 1")
    suspend fun getForDate(date: String): CycleLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(log: CycleLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAll(logs: List<CycleLog>)

    @Query("DELETE FROM cycle_logs WHERE date < :cutoffDate")
    suspend fun deleteBefore(cutoffDate: String): Int
}
