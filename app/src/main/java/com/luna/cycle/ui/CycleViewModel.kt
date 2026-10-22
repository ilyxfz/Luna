package com.luna.cycle.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.luna.cycle.data.CycleLog
import com.luna.cycle.data.CycleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CycleViewModel(private val repository: CycleRepository) : ViewModel() {
    val logs = repository.logs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun save(log: CycleLog) = viewModelScope.launch {
        repository.save(log)
    }

    fun restore(logs: List<CycleLog>) = viewModelScope.launch {
        repository.saveAll(logs)
    }

    fun purgeBefore(cutoffDate: String) = viewModelScope.launch {
        repository.deleteBefore(cutoffDate)
    }

    suspend fun rotateEncryptionKey(context: android.content.Context) =
        repository.rotateEncryptionKey(context)

    suspend fun getForDate(date: String) = repository.getForDate(date)

    companion object {
        fun factory(repository: CycleRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(CycleViewModel::class.java))
                return CycleViewModel(repository) as T
            }
        }
    }
}
