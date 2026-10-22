package com.luna.cycle.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.luna.cycle.R

class QuickLogWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        widgetIds.forEach { id -> updateWidget(context, manager, id) }
    }

    companion object {
        const val ACTION_WATER = "com.luna.cycle.widget.WATER"
        const val ACTION_MOOD = "com.luna.cycle.widget.MOOD"
        const val ACTION_CRAMPS = "com.luna.cycle.widget.CRAMPS"

        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val provider = ComponentName(context, QuickLogWidgetProvider::class.java)
            manager.getAppWidgetIds(provider).forEach { updateWidget(context, manager, it) }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_log)
            mapOf(
                R.id.widget_water to ACTION_WATER,
                R.id.widget_mood to ACTION_MOOD,
                R.id.widget_cramps to ACTION_CRAMPS
            ).forEach { (viewId, action) ->
                val intent = Intent(context, QuickLogActionReceiver::class.java).setAction(action)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    action.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(viewId, pendingIntent)
            }
            manager.updateAppWidget(id, views)
        }
    }
}
