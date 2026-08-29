package com.example.veryweather.data.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.veryweather.MainActivity
import com.example.veryweather.R
import com.example.veryweather.domain.repository.WeatherRepository
import com.example.veryweather.domain.usecase.CheckAlertsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class WeatherAlertWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: WeatherRepository,
    private val checkAlertsUseCase: CheckAlertsUseCase,
    private val sharedPreferences: SharedPreferences
) : CoroutineWorker(context, workerParams) {

    private val heatwaveThreshold
        get() = sharedPreferences.getFloat("heatwave_threshold", 35f).toDouble()

    private val coldThreshold
        get() = sharedPreferences.getFloat("cold_threshold", 0f).toDouble()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val locationResult = repository.getCurrentLocation()
            if (locationResult.isFailure) return@withContext Result.failure()

            val location = locationResult.getOrNull() ?: return@withContext Result.failure()
            val weatherResult = repository.getWeather(location.latitude, location.longitude)
            if (weatherResult.isFailure) return@withContext Result.retry()

            val weatherInfo = weatherResult.getOrNull() ?: return@withContext Result.retry()

            val currentTemp = weatherInfo.currentTemp
            val tempIn6Hours = weatherInfo.hourlyForecast.getOrNull(6)?.temperature

            val alertTypeCurrent = when {
                currentTemp <= coldThreshold -> "FROST"
                currentTemp >= heatwaveThreshold -> "HEATWAVE"
                else -> "NONE"
            }

            val alertType6h = when {
                tempIn6Hours == null -> "NONE"
                tempIn6Hours <= coldThreshold -> "FROST"
                tempIn6Hours >= heatwaveThreshold -> "HEATWAVE"
                else -> "NONE"
            }

            val isAlertActive = alertTypeCurrent != "NONE" || alertType6h != "NONE"

            if (isAlertActive) {
                val lastNotificationTime = sharedPreferences.getLong("last_notification_time", 0L)
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastNotificationTime >= 60 * 60 * 1000) {
                    val alertTypeToNotify = if (alertTypeCurrent != "NONE") alertTypeCurrent else alertType6h
                    val tempToNotify = if (alertTypeCurrent != "NONE") currentTemp else tempIn6Hours!!
                    val useFahrenheit = sharedPreferences.getBoolean("use_fahrenheit", false)
                    val formattedTemp = if (useFahrenheit) "${((tempToNotify * 9 / 5) + 32).toInt()}°F" else "${tempToNotify.toInt()}°C"

                    val title = if (alertTypeToNotify == "FROST") "Alerte Gel" else "Alerte Canicule"
                    val content = "Température de $formattedTemp à ${location.name}."

                    val wasShown = showNotification(title, content, Math.abs(alertTypeToNotify.hashCode()))
                    if (wasShown) {
                        sharedPreferences.edit().putLong("last_notification_time", currentTime).apply()
                    }
                }
            }

            sharedPreferences.edit()
                .putFloat("widget_temp", currentTemp.toFloat())
                .putFloat("widget_temp_6h", tempIn6Hours?.toFloat() ?: -999f)
                .putString("widget_alert_type_current", alertTypeCurrent)
                .putString("widget_alert_type_6h", alertType6h)
                .putString("widget_city_name", location.name.ifBlank { "Unknown" })
                .putInt("widget_weather_code", weatherInfo.weatherCode)
                .putBoolean("widget_is_day", weatherInfo.isDay)
                .apply()

            com.example.veryweather.widget.WeatherWidgetReceiver.updateAll(context)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(title: String, content: String, notificationId: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return false

        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) return false

        val channelId = "weather_alerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertes Météo",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertes de températures extrêmes"
                enableLights(true)
                enableVibration(true)
            }
            val systemNotifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            systemNotifManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(notificationId, notification)
        return true
    }
}
