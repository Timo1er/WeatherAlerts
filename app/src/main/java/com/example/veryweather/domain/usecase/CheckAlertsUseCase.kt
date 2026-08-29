package com.example.veryweather.domain.usecase

import com.example.veryweather.domain.model.WeatherInfo
import javax.inject.Inject

class CheckAlertsUseCase @Inject constructor() {

    fun execute(
        weatherInfo: WeatherInfo,
        heatwaveThreshold: Double,
        coldThreshold: Double = 0.0
    ): AlertResult {
        // Check next 24 hours
        val next24h = weatherInfo.hourlyForecast.take(24)

        val minTemp = next24h.minOfOrNull { it.temperature } ?: return AlertResult.None
        val maxTemp = next24h.maxOfOrNull { it.temperature } ?: return AlertResult.None

        if (minTemp <= coldThreshold) {
            return AlertResult.FrostAlert(minTemp)
        }

        if (maxTemp >= heatwaveThreshold) {
            return AlertResult.HeatwaveAlert(maxTemp)
        }

        return AlertResult.None
    }
}

sealed class AlertResult {
    data class FrostAlert(val minTemp: Double) : AlertResult()
    data class HeatwaveAlert(val maxTemp: Double) : AlertResult()
    data object None : AlertResult()
}
