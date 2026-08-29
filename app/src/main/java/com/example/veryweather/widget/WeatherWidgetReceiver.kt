package com.example.veryweather.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.veryweather.data.worker.WeatherAlertWorker

class WeatherWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeatherWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        
        // Trigger a background update immediately when the widget is added/updated
        val workRequest = OneTimeWorkRequestBuilder<WeatherAlertWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    companion object {
        suspend fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, WeatherWidgetReceiver::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (appWidgetIds.isNotEmpty()) {
                WeatherWidget().updateAll(context)
            }
        }
    }
}
