package com.luna.cycle.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.luna.cycle.data.CycleDatabase
import com.luna.cycle.data.analyzeCycleHistory
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.first

class DailyReminderWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val preferences = applicationContext.getSharedPreferences("luna_settings", Context.MODE_PRIVATE)
        scheduleNext(applicationContext, preferences)
        val dailyReminder = preferences.getBoolean("daily_reminder", false)
        val cycleAlerts = preferences.getBoolean("cycle_alerts", false)
        val medicationReminder = preferences.getBoolean("medication_reminder", false)
        if (!dailyReminder && !cycleAlerts && !medicationReminder) return Result.success()
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return Result.success()
        if (inQuietHours(preferences.getString("quiet_start", "22:00") ?: "22:00",
                preferences.getString("quiet_end", "08:00") ?: "08:00")
        ) return Result.success()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Daily check-in", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val discreet = preferences.getBoolean("discreet_notifications", true)
        val customTitle = if (discreet) {
            preferences.getString("notification_title", null)?.takeIf(String::isNotBlank)
        } else null
        val customMessage = if (discreet) {
            preferences.getString("notification_message", null)?.takeIf(String::isNotBlank)
        } else null
        val notifications = NotificationManagerCompat.from(applicationContext)
        if (dailyReminder) {
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                .setContentTitle(customTitle ?: if (discreet) "A moment for yourself" else "Your Luna check-in")
                .setContentText(customMessage ?: if (discreet) "Your daily update is ready." else "Take a moment to log how you're feeling today.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            notifications.notify(NOTIFICATION_ID, notification)
        }
        if (cycleAlerts) {
            val lastPeriod = preferences.getString("last_period", null)
                ?.let(LocalDate::parse) ?: LocalDate.now().minusDays(8)
            val logs = CycleDatabase.getInstance(applicationContext).cycleLogDao().observeAll().first()
            val useAverage = preferences.getBoolean("use_cycle_average", false)
            val configuredLength = preferences.getInt("custom_cycle_length", 28)
            val history = analyzeCycleHistory(
                logs,
                lastPeriod,
                if (useAverage) 0 else configuredLength,
                preferences.getInt("cycle_average_window", 3)
            )
            val cycleLength = history.averageCycleLength?.takeIf { useAverage } ?: configuredLength
            val cycleStart = history.periodStarts.lastOrNull { !it.isAfter(LocalDate.now()) } ?: lastPeriod
            val elapsedDays = ChronoUnit.DAYS.between(cycleStart, LocalDate.now()).toInt()
            val cycleDay = ((elapsedDays % cycleLength) + cycleLength) % cycleLength + 1
            val ovulationDay = (cycleLength - 14).coerceAtLeast(10)
            val cycleMessage = when {
                elapsedDays > cycleLength + 7 ->
                    if (discreet) "Your daily update is ready." else "Your period is later than its estimate."
                cycleDay == cycleLength - 2 || cycleDay == cycleLength - 1 ->
                    if (discreet) "Your daily update is ready." else "Your period may arrive in about ${cycleLength - cycleDay + 1} days."
                cycleDay in (ovulationDay - 5)..(ovulationDay + 1) ->
                    if (discreet) "Your daily update is ready." else "Your fertile window may be approaching."
                else -> null
            }
            if (cycleMessage != null) {
                val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                    .setContentTitle(customTitle ?: if (discreet) "A moment for yourself" else "Your cycle update")
                    .setContentText(customMessage ?: cycleMessage)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()
                notifications.notify(CYCLE_NOTIFICATION_ID, notification)
            }
        }
        if (medicationReminder) {
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                .setContentTitle(customTitle ?: if (discreet) "A moment for yourself" else "Medication reminder")
                .setContentText(customMessage ?: if (discreet) "Your daily update is ready." else "It's time for your medication.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            notifications.notify(MEDICATION_NOTIFICATION_ID, notification)
        }
        return Result.success()
    }

    private fun inQuietHours(start: String, end: String): Boolean {
        val startTime = java.time.LocalTime.parse(start)
        val endTime = java.time.LocalTime.parse(end)
        if (startTime == endTime) return false
        val now = java.time.LocalTime.now()
        return if (startTime.isBefore(endTime)) {
            !now.isBefore(startTime) && now.isBefore(endTime)
        } else {
            !now.isBefore(startTime) || now.isBefore(endTime)
        }
    }

    private fun scheduleNext(context: Context, preferences: android.content.SharedPreferences) {
        if (!preferences.getBoolean("daily_reminder", false) &&
            !preferences.getBoolean("cycle_alerts", false) &&
            !preferences.getBoolean("medication_reminder", false)
        ) return
        val alertTime = java.time.LocalTime.parse(preferences.getString("alert_time", "20:00") ?: "20:00")
        val now = java.time.LocalDateTime.now()
        val nextAlert = now.toLocalDate().atTime(alertTime).let { if (it.isAfter(now)) it else it.plusDays(1) }
        val delay = java.time.Duration.between(now, nextAlert).toMillis().coerceAtLeast(0)
        val request = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "luna-daily-reminder",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }

    companion object {
        private const val CHANNEL_ID = "luna_daily_check_in"
        private const val NOTIFICATION_ID = 42
        private const val CYCLE_NOTIFICATION_ID = 43
        private const val MEDICATION_NOTIFICATION_ID = 44
    }
}
