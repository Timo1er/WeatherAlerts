package com.example.veryweather.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.veryweather.data.worker.WeatherAlertWorker

class UnlockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            val workRequest = OneTimeWorkRequestBuilder<WeatherAlertWorker>().build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
