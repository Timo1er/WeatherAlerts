package com.example.veryweather.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeather,
    val hourly: HourlyWeather
)

@Serializable
data class CurrentWeather(
    val time: String,
    val temperature_2m: Double,
    val apparent_temperature: Double,
    val is_day: Int,
    val weather_code: Int
)

@Serializable
data class HourlyWeather(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val weather_code: List<Int>
)
