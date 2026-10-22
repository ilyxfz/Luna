package com.luna.cycle.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.luna.cycle.data.CycleDatabase
import com.luna.cycle.data.CycleLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class QuickLogActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = CycleDatabase.getInstance(context).cycleLogDao()
                val date = LocalDate.now().toString()
                val existing = dao.getForDate(date) ?: CycleLog(date = date)
                val updated = when (intent.action) {
                    QuickLogWidgetProvider.ACTION_WATER -> existing.copy(
                        waterGlasses = (existing.waterGlasses + 1).coerceAtMost(12)
                    )
                    QuickLogWidgetProvider.ACTION_MOOD -> existing.copy(mood = if (existing.mood == "Calm") "" else "Calm")
                    QuickLogWidgetProvider.ACTION_CRAMPS -> {
                        val symptoms = existing.symptomSet()
                        existing.copy(symptoms = (if ("Cramps" in symptoms) symptoms - "Cramps" else symptoms + "Cramps").sorted().joinToString("|"))
                    }
                    else -> return@launch
                }
                dao.save(updated)
                QuickLogWidgetProvider.refresh(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
