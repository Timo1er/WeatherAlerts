package com.example.veryweather.data.network

import com.example.veryweather.data.model.OpenMeteoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun getWeatherForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,apparent_temperature,is_day,weather_code",
        @Query("hourly") hourly: String = "temperature_2m,weather_code",
        @Query("forecast_days") forecastDays: Int = 2,
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse
}
