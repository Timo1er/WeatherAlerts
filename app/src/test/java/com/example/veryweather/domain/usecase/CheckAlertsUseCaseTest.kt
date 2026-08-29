package com.example.veryweather.domain.usecase

import com.example.veryweather.domain.model.HourlyData
import com.example.veryweather.domain.model.WeatherInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckAlertsUseCaseTest {
    @Test
    fun `cold alert should use configured threshold`() {
        val weatherInfo = WeatherInfo(
            currentTemp = 2.0,
            apparentTemp = 1.0,
            weatherCode = 0,
            isDay = true,
            hourlyForecast = listOf(
                HourlyData("2024-01-01T00:00", -3.0, 0),
                HourlyData("2024-01-01T01:00", 1.0, 0),
                HourlyData("2024-01-01T02:00", 3.0, 0)
            )
        )

        val result = CheckAlertsUseCase().execute(weatherInfo, 35.0, 0.0)

        assertTrue(result is AlertResult.FrostAlert)
        assertEquals(-3.0, (result as AlertResult.FrostAlert).minTemp, 0.0)
    }
}
