package com.example.veryweather.domain.repository

import com.example.veryweather.domain.model.LocationModel
import com.example.veryweather.domain.model.WeatherInfo

interface WeatherRepository {
    suspend fun getWeather(lat: Double, lon: Double): Result<WeatherInfo>
    suspend fun getCurrentLocation(): Result<LocationModel>
}
