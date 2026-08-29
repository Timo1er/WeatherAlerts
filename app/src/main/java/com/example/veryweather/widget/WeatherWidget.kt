package com.example.veryweather.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.veryweather.MainActivity
import com.example.veryweather.theme.FrostBlue
import com.example.veryweather.theme.HeatwaveRed

class WeatherWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences("veryweather_prefs", Context.MODE_PRIVATE)
        val currentTemp = if (prefs.contains("widget_temp")) prefs.getFloat("widget_temp", 0f).toDouble() else null
        val temp6hRaw = prefs.getFloat("widget_temp_6h", -999f)
        val temp6h = if (temp6hRaw != -999f) temp6hRaw.toDouble() else null
        val cityName = prefs.getString("widget_city_name", "Paris") ?: "Paris"
        val weatherCode = prefs.getInt("widget_weather_code", -1)
        val isDay = prefs.getBoolean("widget_is_day", true)
        val useFahrenheit = prefs.getBoolean("use_fahrenheit", false)

        val alertTypeCurrent = prefs.getString("widget_alert_type_current", "NONE") ?: "NONE"
        val alertType6h = prefs.getString("widget_alert_type_6h", "NONE") ?: "NONE"

        val alertType = when {
            alertTypeCurrent == "HEATWAVE" || alertType6h == "HEATWAVE" -> "HEATWAVE"
            alertTypeCurrent == "FROST" || alertType6h == "FROST" -> "FROST"
            else -> "NONE"
        }

        val weatherIcon = weatherCodeToEmoji(weatherCode, isDay)

        val formattedCurrent = currentTemp?.let {
            if (useFahrenheit) "${((it * 9 / 5) + 32).toInt()}°F" else "${it.toInt()}°C"
        } ?: "--°C"

        val formatted6h = temp6h?.let {
            if (useFahrenheit) "${((it * 9 / 5) + 32).toInt()}°F" else "${it.toInt()}°C"
        } ?: "--°C"

        provideContent {
            GlanceTheme {
                val bgColor = when (alertType) {
                    "FROST" -> ColorProvider(day = FrostBlue, night = FrostBlue)
                    "HEATWAVE" -> ColorProvider(day = HeatwaveRed, night = HeatwaveRed)
                    else -> GlanceTheme.colors.background
                }

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(bgColor)
                        .padding(8.dp)
                        .clickable(androidx.glance.appwidget.action.actionStartActivity(android.content.Intent(context, MainActivity::class.java))),
                    contentAlignment = Alignment.Center
                ) {
                    // Icon + temperatures centered
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = weatherIcon,
                            style = TextStyle(fontSize = 36.sp)
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = formattedCurrent,
                            style = TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlanceTheme.colors.onBackground
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = formatted6h,
                            style = TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Normal,
                                color = GlanceTheme.colors.onBackground
                            )
                        )
                    }
                    
                    // City pinned to bottom-left
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(
                            text = cityName,
                            modifier = GlanceModifier.padding(4.dp),
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = GlanceTheme.colors.onBackground
                            )
                        )
                    }
                }
            }
        }
    }

    private fun weatherCodeToEmoji(code: Int, isDay: Boolean): String = when (code) {
        0 -> if (isDay) "☀️" else "🌙"
        1 -> if (isDay) "🌤️" else "🌙"
        2 -> "⛅"
        3 -> "☁️"
        45, 48 -> "🌫️"
        51, 53, 55 -> "🌦️"
        56, 57 -> "🌧️"
        61, 63, 65 -> "🌧️"
        66, 67 -> "🌨️"
        71, 73, 75, 77 -> "❄️"
        80, 81, 82 -> "🌦️"
        85, 86 -> "🌨️"
        95 -> "⛈️"
        96, 99 -> "⛈️"
        else -> if (isDay) "🌤️" else "🌙"
    }
}
