package com.luna.cycle.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class CycleRepository(private val database: CycleDatabase) {
    private val currentDatabase = MutableStateFlow(database)
    val logs: Flow<List<CycleLog>> = currentDatabase.flatMapLatest { it.cycleLogDao().observeAll() }

    suspend fun getForDate(date: String): CycleLog? = currentDatabase.value.cycleLogDao().getForDate(date)

    suspend fun save(log: CycleLog) = currentDatabase.value.cycleLogDao().save(log.copy(updatedAt = System.currentTimeMillis()))

    suspend fun saveAll(logs: List<CycleLog>) = currentDatabase.value.cycleLogDao().saveAll(logs)

    suspend fun deleteBefore(cutoffDate: String) = currentDatabase.value.cycleLogDao().deleteBefore(cutoffDate)

    fun rotateEncryptionKey(context: android.content.Context) {
        currentDatabase.value = CycleDatabase.rotateEncryptionKey(context)
    }
}
