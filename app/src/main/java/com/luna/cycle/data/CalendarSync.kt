package com.luna.cycle.data

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

object CalendarSync {
    private const val EVENT_MARKER = "luna://cycle-prediction"

    fun sync(context: Context, startDate: LocalDate, cycleLength: Int, discreet: Boolean): Int {
        require(cycleLength in 15..60) { "Cycle length must be between 15 and 60 days." }
        require(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
        ) { "Calendar access has not been granted." }

        val calendarProjection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL)
        val writableCalendars = mutableListOf<Long>()
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            calendarProjection,
            "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ?",
            arrayOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR.toString()),
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            while (cursor.moveToNext()) writableCalendars += cursor.getLong(idColumn)
        } ?: error("Unable to read available calendars.")
        require(writableCalendars.isNotEmpty()) { "No writable calendar is available on this device." }

        val existingIds = mutableListOf<Long>()
        context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            arrayOf(CalendarContract.Events._ID),
            "${CalendarContract.Events.CUSTOM_APP_URI} = ?",
            arrayOf(EVENT_MARKER),
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(CalendarContract.Events._ID)
            while (cursor.moveToNext()) existingIds += cursor.getLong(idColumn)
        } ?: error("Unable to read existing Luna calendar entries.")
        existingIds.forEach { eventId ->
            context.contentResolver.delete(
                CalendarContract.Events.CONTENT_URI,
                "${CalendarContract.Events._ID} = ? AND ${CalendarContract.Events.CUSTOM_APP_URI} = ?",
                arrayOf(eventId.toString(), EVENT_MARKER)
            )
        }

        val calendarId = writableCalendars.first()
        var added = 0
        val daysSinceStart = ChronoUnit.DAYS.between(startDate, LocalDate.now()).coerceAtLeast(0)
        val completedCycles = daysSinceStart / cycleLength
        repeat(3) { index ->
            val date = startDate.plusDays(cycleLength.toLong() * (completedCycles + index + 1))
            val startMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, startMillis + 86_400_000L)
                put(CalendarContract.Events.ALL_DAY, 1)
                put(CalendarContract.Events.EVENT_TIMEZONE, "UTC")
                put(CalendarContract.Events.TITLE, if (discreet) "Personal event" else "Predicted cycle start")
                put(CalendarContract.Events.DESCRIPTION, if (discreet) "Personal reminder." else "Estimated from your logged cycle. This prediction may be inaccurate.")
                put(CalendarContract.Events.CUSTOM_APP_URI, EVENT_MARKER)
            }
            requireNotNull(context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)) {
                "Unable to add a predicted cycle date to the calendar."
            }
            added++
        }
        return added
    }
}
