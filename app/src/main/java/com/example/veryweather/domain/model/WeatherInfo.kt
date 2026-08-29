package com.example.veryweather.domain.model

data class WeatherInfo(
    val currentTemp: Double,
    val apparentTemp: Double,
    val weatherCode: Int,
    val isDay: Boolean,
    val hourlyForecast: List<HourlyData>,
    val cityName: String = ""
)

data class HourlyData(
    val time: String,
    val temperature: Double,
    val weatherCode: Int
)
