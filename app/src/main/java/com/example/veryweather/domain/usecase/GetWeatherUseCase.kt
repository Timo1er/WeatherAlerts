package com.example.veryweather.domain.usecase

import com.example.veryweather.domain.model.WeatherInfo
import com.example.veryweather.domain.repository.WeatherRepository
import javax.inject.Inject

class GetWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend fun execute(): Result<WeatherInfo> {
        val locationResult = repository.getCurrentLocation()
        if (locationResult.isFailure) return Result.failure(locationResult.exceptionOrNull()!!)
        
        val location = locationResult.getOrNull()!!
        return repository.getWeather(location.latitude, location.longitude).map {
            it.copy(cityName = location.name)
        }
    }
}
