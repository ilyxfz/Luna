package com.luna.cycle.reminders

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.luna.cycle.data.CycleDatabase
import java.time.LocalDate

class RetentionWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = try {
        val years = applicationContext.getSharedPreferences("luna_settings", Context.MODE_PRIVATE)
            .getInt("retention_years", 0)
        if (years > 0) {
            CycleDatabase.getInstance(applicationContext).cycleLogDao()
                .deleteBefore(LocalDate.now().minusYears(years.toLong()).toString())
        }
        Result.success()
    } catch (error: Exception) {
        Log.e("LunaRetention", "Unable to apply the local data retention policy", error)
        Result.retry()
    }
}
